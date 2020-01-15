package de.julielab.ir.experiments.ablation;

public interface AblationLatexTableInfo {
    boolean indent(String ablationName);

    boolean addMidruleAfter(String ablationName);
}
