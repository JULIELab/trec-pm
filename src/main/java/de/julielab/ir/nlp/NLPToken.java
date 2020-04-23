package de.julielab.ir.nlp;

public class NLPToken {
    private String token;
    private String posTag;

    public NLPToken(String token, String posTag) {
        this.token = token;
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
