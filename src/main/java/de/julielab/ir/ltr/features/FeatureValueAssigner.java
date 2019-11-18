package de.julielab.ir.ltr.features;

import cc.mallet.types.Instance;

import java.io.Serializable;
import java.util.function.Consumer;

public interface FeatureValueAssigner extends Consumer<Instance>, Serializable {
}
