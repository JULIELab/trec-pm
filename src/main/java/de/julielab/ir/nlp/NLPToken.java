package de.julielab.ir.nlp;

public class NLPToken {
    private String token;
    private String lemma;
    private String posTag;

    public String getLemma() {
        return lemma;
    }

    public NLPToken(String token, String lemma, String posTag) {
        this.token = token;
        this.lemma = lemma;
        this.posTag = posTag;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPosTag() {
        return posTag;
    }

    public void setPosTag(String posTag) {
        this.posTag = posTag;
    }
}
