package de.julielab.ir.ltr.features;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class FeatureUtilsTest {
    @Test
    public void testNormalizeString() throws IOException {
        String input = "A specific human lysophospholipase: cDNA cloning, tissue distribution and kinetic characterization.\n" +
                "Lysophospholipases are critical enzymes that act on biological membranes to regulate the multifunctional lysophospholipids; increased levels of lysophospholipids are associated with a host of diseases. Herein we report the cDNA cloning of a human brain 25 kDa lysophospholipid-specific lysophospholipase (hLysoPLA). The enzyme (at both mRNA and protein levels) is widely distributed in tissues, but with quite different abundances. The hLysoPLA hydrolyzes lysophosphatidylcholine in both monomeric and micellar forms, and exhibits apparent cooperativity and surface dilution kinetics, but not interfacial activation. Detailed kinetic analysis indicates that the hLysoPLA binds first to the micellar surface and then to the substrate presented on the surface. The kinetic parameters associated with this surface dilution kinetic model are reported, and it is concluded that hLysoPLA has a single substrate binding site and a surface recognition site. The apparent cooperativity observed is likely due to the change of substrate presentation. In contrast to many non-specific lipolytic enzymes that exhibit lysophospholipase activity, hLysoPLA hydrolyzes only lysophospholipids and has no other significant enzymatic activity. Of special interest, hLysoPLA does not act on plasmenylcholine. Of the several inhibitors tested, only methyl arachidonyl fluorophosphonate (MAFP) potently and irreversibly inhibits the enzymatic activity. The inhibition by MAFP is consistent with the catalytic mechanism proposed for the enzyme - a serine hydrolase with a catalytic triad composed of Ser-119, Asp-174 and His-208.";
        final String normalizedString = FeatureUtils.getInstance().normalizeString(input);
        assertEquals("specif human lysophospholipas cdna clone tissu distribut kinet character lysophospholipas critic enzym act biolog membran regul multifunct lysophospholipid increas level lysophospholipid associ host diseas report cdna clone human brain 25 kda lysophospholipid specif lysophospholipas hlysopla enzym mrna protein level wide distribut tissu quit differ abund hlysopla hydrolyz lysophosphatidylcholin monomer micellar form exhibit appar cooper surfac dilut kinet interfaci activ detail kinet analysi indic hlysopla bind micellar surfac substrat present surfac kinet paramet associ surfac dilut kinet model report conclud hlysopla singl substrat bind site surfac recognit site appar cooper observ like chang substrat present contrast non specif lipolyt enzym exhibit lysophospholipas activ hlysopla hydrolyz lysophospholipid signific enzymat activ special hlysopla doe act plasmenylcholin inhibitor test methyl arachidonyl fluorophosphon mafp potent irrevers inhibit enzymat activ inhibit mafp consist catalyt mechan propos enzym serin hydrolas catalyt triad compos ser 119 asp 174 208", normalizedString);
    }
}
