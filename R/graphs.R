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

# Input folder
# setwd("docs/2017/final-results/clinical-trials")
# setwd("docs/2018/final-results/biomedical-articles")
setwd("stats")

# Note that official metrics are generated with an older version of trec_eval that uses different names for metrics.
# trec_eval 8.1: "P10", "R-prec", "ircl_prn."
# trec_eval 9.0+: "P_10", "Rprec", "iprec_at_recall_"
# metrics <- c("P5", "P10", "P15")
# metrics <- c("infNDCG", "P10", "R-prec")
metrics <- c("ndcg", "P_10", "set_recall")
# iprec_at_recall <- "ircl_prn."
iprec_at_recall <- "iprec_at_recall_"

# Use .trec_eval files to discover run ids, since they're always present (including 2017 CT).
run_ids <- gsub(".trec_eval", "", list.files(pattern="*.trec_eval"))


# Best, median and worst are averages over all topics
# TODO get always metrics from 2017 and 2018 and merge for plots per topic
stats_file <- list.files(pattern="*.stats")[0]
if (file.exists(stats_file)) {
  stats <- read.table(stats_file, skip=5)
  metrics_best <- c(colMeans(stats)[2], colMeans(stats)[5], colMeans(stats)[8])
  metrics_median <- c(colMeans(stats)[3], colMeans(stats)[6], colMeans(stats)[9])
  metrics_worst <- c(colMeans(stats)[4], colMeans(stats)[7], colMeans(stats)[10])
}

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


### Boxplots ###
boxplots <- function(results) {
  plots <- list()
  for (i in metrics) {
    test <- results %>%
      filter(measure==i,topic!='all') %>%
      mutate(value = as.numeric(value))
  
    means <- aggregate(value ~ run, test, mean)
  
    g <- ggplot(test, aes(x=run, y=value, fill=run)) +
      geom_boxplot(alpha=0.8) +
      # scale_fill_manual(values=tug_colors) +
      xlab("") +
      ylab(i) +
      theme_linedraw() +
      theme(axis.text.x = element_text(angle = 45, vjust = 1, hjust = 1),
            plot.background = element_blank(),
            panel.background = element_blank()) +
  
      stat_summary(fun.y=mean, color=extra_color, geom="point", shape=18, size=2,show.legend = FALSE) +
      #geom_text(data = means, aes(label = round(value,4), y = value + 0.04)) +
      guides(fill=FALSE) + scale_y_continuous(breaks=seq(0,1,0.1), limits=c(0,1))
  
    if (exists("stats")) {
      g <- g + geom_hline(yintercept=metrics_best[i], linetype="dashed", color = extra_color)
      # + geom_text(aes(0.6,metrics_best[i],label="best", vjust = 1), color=extra_color)
  
      g <- g + geom_hline(yintercept=metrics_median[i], linetype="dashed", color = extra_color)
      # + geom_text(aes(0.7,metrics_median[i],label="median", vjust = 1), color=extra_color)
  
      # g <- g + geom_hline(yintercept=metrics_worst[i], linetype="dashed", color = extra_color)
      # + geom_text(aes(0.7,metrics_worst[i],label="worst", vjust = 1), color=extra_color)
    }
  
    plots <- append(plots, list(g))
    #g
  }
  
  pdf(file = "boxplots.pdf", width = 7, height = 3.5)
  grid.arrange(grobs = plots, nrow = 1, ncol = length(metrics))
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
  
    if (exists("stats")) {
      g <- g + geom_line(data=stats,
                         aes(x=stats[,1], y=stats[,i*3-1], group=NULL),  # Best
                         size=0.5,
                         color=extra_color) +
        geom_line(data=stats,
                  aes(x=stats[,1], y=stats[,i*3], group=NULL),      # Median
                  size=0.5,
                  color=extra_color)
    }
    
    plots <- append(plots, list(g))
  }
  
  # Add legend only to the last plot to save space
  plots[length(plots)][[1]] <- plots[length(plots)][[1]] + theme(legend.position = "bottom")
  
  pdf(file = "topics.pdf", width = 10, height = 14)
  grid.arrange(grobs = plots, nrow = length(metrics), ncol = 1)
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
