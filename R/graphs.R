library(dplyr)
library(ggplot2)
library(gridExtra)
library(stringr)

#mug_green <- "#007A25"
tug_red <- "#ff0a6e"
tug_blue <- "#4f82bd"
tug_green <- "#9cba59"
tug_magenta <- "#8063a3"
tug_orange <- "#f79645"
extra_color <- "darkred"
# tug_colors <- c(hpipubboost=tug_red, hpipubnone=tug_blue, hpipubbase=tug_green, hpipubclass=tug_magenta, hpipubcommon=tug_orange,
#                     hpictboost=tug_red, hpictphrase=tug_blue, hpictbase=tug_green, hpictall=tug_magenta, hpictcommon=tug_orange)

# XXX Set metrics
# Note that official metrics are generated with an older version of trec_eval that uses different names for metrics.
# TODO normalize all metrics to 9.0 format
# trec_eval 8.1: "P10", "R-prec", "ircl_prn."
# trec_eval 9.0+: "P_10", "Rprec", "iprec_at_recall_"
# metrics <- c("P5", "P10", "P15")
metrics <- c("infNDCG", "P_10", "Rprec")      # Default metrics
# metrics <- c("ndcg", "P_10", "set_recall")  # Michel's thesis
# iprec_at_recall <- "ircl_prn."
iprec_at_recall <- "iprec_at_recall_"

# XXX Set input folder
# input_folder <- "docs/2017/final-results/clinical-trials"
# input_folder <- "docs/2018/final-results/biomedical-articles"
input_folder <- "stats"

# XXX Set year. Optional when .trec_eval files already contain topics in the format year-topic (e.g. "201712").
# year <- 2017
# year <- 2018
year <- ""

# XXX Set task
# task_name <- "ct"
task_name <- "ba"

old_wd = getwd()

### Statistics of all participants runs ###
collect_stats <- function() {
  stats_files <- list.files("R", pattern="*.stats")
  
  stats <- data.frame()
  for (stats_file in stats_files) {
    matches <- str_match(stats_file, "([0-9]{4})-([a-z]{2}).stats")
    year <- matches[2]
    task <- matches[3]
    
    file_stats <- read.table(paste("R", stats_file, sep="/"), skip=5)
    names(file_stats) <- c("topic", "infNDCG.best", "infNDCG.median", "infNDCG.worst",
                           "P_10.best", "P_10.median", "P_10.worst",
                           "Rprec.best", "Rprec.median", "Rprec.worst")
    if (year == 2017 && task == "ct") {
      names(file_stats) <- c("topic", "P_5.best", "P_5.median", "P_5.worst",
                             "P_10.best", "P_10.median", "P_10.worst",
                             "P_15.best", "P_15.median", "P_15.worst")
    }
    
    # Add task and year
    file_stats <- file_stats %>%
      mutate(task = task) %>%
      mutate(year = year) %>%
      mutate(yeartopic = paste0(year, sprintf("%1d", topic))) # FIXME %02d
    
    stats <- stats %>% bind_rows(file_stats)
  }
  stats$yeartopic <- as.factor(stats$yeartopic)
  
  return(stats)
}
stats <- collect_stats()
task_stats <- filter(stats, task == task_name)

### Results ###
setwd(input_folder)

# Use .trec_eval files to discover run ids, since they're always present (including 2017 CT).
run_ids <- gsub(".trec_eval", "", list.files(pattern="*.trec_eval"))

collect_results <- function(run_ids) {
  results <- data.frame()
  for(run_id in run_ids) {
    tmp <- read.table(paste(run_id, ".trec_eval", sep=""), header = FALSE, colClasses = "character")
    tmp <- tmp %>% mutate(run_id)
    names(tmp) <- c('measure', 'topic', 'value', 'run')
    results <- results %>%  bind_rows(tmp)
    remove(tmp)
  
    sample_file <- paste(run_id, ".sampleval", sep="")
    if (file.exists(sample_file)) {
      tmp <- read.table(paste(run_id, ".sampleval", sep=""), header = FALSE, colClasses = "character")
      tmp <- tmp %>% mutate(run_id)
      names(tmp) <- c('measure', 'topic', 'value', 'run')
      results <- results %>%  bind_rows(tmp)
      remove(tmp)
    }
  }
  # Fix as factors for printing correctly
  results$run <- factor(results$run, levels=run_ids)
  return(results)
}
results <- collect_results(run_ids)

### Boxplots ###
boxplots <- function(results) {
  plots <- list()
  for (metric in metrics) {
    test <- results %>%
      filter(measure==metric,topic!='all') %>%
      mutate(value = as.numeric(value))
    
    # Skip metric if data is not available (e.g. infNDCG for CT 2017)
    if (nrow(test) == 0) {
      next
    }
  
    means <- aggregate(value ~ run, test, mean)
  
    g <- ggplot(test, aes(x=run, y=value, fill=run)) +
      geom_boxplot(alpha=0.8) +
      # scale_fill_manual(values=tug_colors) +
      xlab("") +
      ylab(metric) +
      theme_linedraw() +
      theme(axis.text.x = element_text(angle = 45, vjust = 1, hjust = 1),
            plot.background = element_blank(),
            panel.background = element_blank()) +
  
      stat_summary(fun.y=mean, color=extra_color, geom="point", shape=18, size=2,show.legend = FALSE) +
      #geom_text(data = means, aes(label = round(value,4), y = value + 0.04)) +
      guides(fill=FALSE) + scale_y_continuous(breaks=seq(0,1,0.1), limits=c(0,1))
  
    best_column <- paste0(metric, ".best")
    if (best_column %in% colnames(task_stats)) {
      average_best <- mean(task_stats[best_column][,1])
      g <- g + geom_hline(yintercept=average_best, linetype="dashed", color = extra_color)
    }
    
    median_column <- paste0(metric, ".median")
    if (median_column %in% colnames(task_stats)) {
      average_median <- mean(task_stats[median_column][,1])
      g <- g + geom_hline(yintercept=average_median, linetype="dashed", color = extra_color)
    }
  
    plots <- append(plots, list(g))
  }
  
  pdf(file = "boxplots.pdf", width = 2.33 * length(plots), height = 3.5)
  grid.arrange(grobs = plots, nrow = 1, ncol = length(plots))
  dev.off()
}
boxplots(results)

### Graphs per Topic ###
topic_plots <- function(results) {
  plots <- list()
  for (metric in metrics) {
    results_per_topic <- results %>%
      filter(measure==metric,topic!='all') %>%
      mutate(value = as.numeric(value)) %>%
      mutate(topic = factor(topic, levels=unique(sort(as.numeric(topic))))) # Sort topics
    
    # Skip metric if data is not available (e.g. infNDCG for CT 2017)
    if (nrow(results_per_topic) == 0) {
      next
    }
  
    g <- ggplot(data=results_per_topic, aes(x=topic, y=value, group=run)) +
      geom_line(aes(color=run, linetype=run)) +
      xlab("Topic") +
      ylab(metric) +
      theme_linedraw() +
      scale_y_continuous(breaks=seq(0,1,0.1), minor_breaks=NULL, limits=c(0,1.05)) +  # infNDCG can be higher than 1.00
      theme(axis.text.x = element_text(angle = 90, hjust = 1),
            plot.background = element_blank(),
            panel.background = element_blank(),
            legend.position = "none",
            legend.title = element_blank())
  
    best_column <- paste0(metric, ".best")
    if (best_column %in% colnames(task_stats)) {
      best_column <- sym(best_column)
      g <- g + geom_line(data=task_stats,
                         aes(x=yeartopic, y=!!best_column, group="best"),  # Best
                         size=0.5,
                         color=extra_color)
    }
    
    median_column <- paste0(metric, ".median")
    if (median_column %in% colnames(task_stats)) {
      median_column <- sym(median_column)
      g <- g + geom_line(data=task_stats,
                                 aes(x=yeartopic, y=!!median_column, group="median"),      # Median
                                 size=0.5,
                                 color=extra_color)
    }
    
    plots <- append(plots, list(g))
  }
  
  # Add legend only to the last plot to save space
  plots[length(plots)][[1]] <- plots[length(plots)][[1]] + theme(legend.position = "bottom")
  
  pdf(file = "topics.pdf", width = 10, height = 4.67 * length(plots))
  grid.arrange(grobs = plots, nrow = length(plots), ncol = 1)
  dev.off()
}
topic_plots(results)

### Precision - Recall Graphs ###
precision_recall <- function(results) {
  ircl <- results %>%
    filter(str_detect(measure, iprec_at_recall),topic=='all') %>%    # Filter only interpolated recall data
    mutate(measure=str_replace(measure, iprec_at_recall, "")) %>%   # Remove prefix
    mutate(measure=str_trunc(measure, 3, ellipsis="")) %>%       # Remove second decimal place
    mutate(value = as.numeric(value))
  
  pdf(file = paste("precision-recall.pdf", sep=""), width = 7, height = 3.5)
  g <- ggplot(data=ircl, aes(x=measure, y=value, group=run)) +
    geom_line(aes(color=run, linetype=run)) +
    geom_point(size=0.5) +
    scale_y_continuous(breaks=seq(0,1,0.1), minor_breaks=NULL,limits=c(0,1)) +
    xlab("Recall") +
    ylab("Precision") +
    theme_linedraw() +
    theme(plot.background = element_blank(), panel.background = element_blank(), legend.title = element_blank())
  print(g)
  dev.off()
}
precision_recall(results)

setwd(old_wd)
