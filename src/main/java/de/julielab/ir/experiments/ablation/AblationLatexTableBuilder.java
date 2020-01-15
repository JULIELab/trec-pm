package de.julielab.ir.experiments.ablation;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Map;

import static de.julielab.ir.paramopt.HttpParamOptServer.INFNDCG;

public class AblationLatexTableBuilder {
    @NotNull
    public static StringBuilder buildLatexTable(Map<String, AblationCrossValResult> topDownResults, Map<String, AblationCrossValResult> bottomUpResults, String caption, String label, AblationLatexTableInfo topDownTableInfo, AblationLatexTableInfo bottomUpTableInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("\\begin{table}[htp]\n" +
                "\\caption{"+caption+"}\n" +
                "\\begin{center}\n" +
                "\\begin{tabular}{l|c|c}\n" +
                "\\toprule\n" +
                "configuration & infNDCG & diff to ref \\\\\n" +
                "\\midrule\n" +
                "\\multicolumn{3}{c}{\\textbf{top-down}} \\\\\n" +
                "\\midrule\n");
        appendReferenceTableLine(sb, "ALL (ref)", topDownResults.values().iterator().next());
        sb.append("\\midrule\n");
        for (AblationCrossValResult r : topDownResults.values()) {
            if (topDownTableInfo != null && topDownTableInfo.indent(r.getAblationGroupName()))
                sb.append("\\quad");
            getAblationResultTableLine(sb, r);
            if (topDownTableInfo != null && topDownTableInfo.addMidruleAfter(r.getAblationGroupName()))
                sb.append("\\midrule\n");
        }
        sb.append("\\midrule\n" +
                "\\multicolumn{3}{c}{\\textbf{bottom-up}} \\\\\n" +
                "\\midrule\n");
        appendReferenceTableLine(sb, "DISMAX (ref)", bottomUpResults.values().iterator().next());
        sb.append("\\midrule\n");
        for (AblationCrossValResult r : bottomUpResults.values()) {
            if (bottomUpTableInfo != null && bottomUpTableInfo.indent(r.getAblationGroupName()))
                sb.append("\\quad");
            getAblationResultTableLine(sb, r);
            if (bottomUpTableInfo != null && bottomUpTableInfo.addMidruleAfter(r.getAblationGroupName()))
                sb.append("\\midrule\n");
        }
        sb.append("\\bottomrule\n" +
                "\\end{tabular}\n" +
                "\\end{center}\n" +
                "\\label{"+label+"}\n" +
                "\\end{table}");
        return sb;
    }

    private static void getAblationResultTableLine(StringBuilder sb, AblationCrossValResult crossValResult) {
        DecimalFormat df = new DecimalFormat("#,###,###,##0.00");
        double ablationScore = crossValResult.getMeanAblationScore(INFNDCG);
        double referenceScore = crossValResult.getMeanReferenceScore(INFNDCG);
        double diff = referenceScore - ablationScore;
        double pct = diff / referenceScore * 100;
        sb.append(crossValResult.get(0).getAblationName()).append(" & ").append(df.format(ablationScore)).append(" & $");
        if (pct < 0)
            sb.append("+");
        sb.append(df.format(pct * -1)).append("\\%$").append(" \\\\").append("\n");
    }

    private static void appendReferenceTableLine(StringBuilder sb, String referenceName, AblationCrossValResult crossValResult) {
        DecimalFormat df = new DecimalFormat("#,###,###,##0.00");
        double score = crossValResult.getMeanReferenceScore(INFNDCG);

        sb.append(referenceName).append("  & ").append(df.format(score)).append(" & ").append("$0.0\\%$").append(" \\\\").append("\n");
    }
}
