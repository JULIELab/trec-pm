def mapRunNames(tup):
    #names = {"baseline":"BASE", "dgint":"DGINT", "dishyper":"DIS_HYP", "dissyn":"DIS_SYN", "dissynpt":"DIS_SYN_PT", "dissynpthyper": "DIS_ALL", "gendis": "GEN_DIS_ALL", "gendisdgint":"ALL", "gensyn":"GEN_SYN", "gensyndesc":"GEN_SYN_DES", "gensyndescplus":"GEN_SYN_DES_PLUS"}
    names = {"DSYN": "DS", "DPT": "DP", "DHYP": "DH", "GSYN": "GS", "GDES": "GD"}
    if type(tup) == str:
        return replaceAcros(tup)
    return (replaceAcros(tup[0]),)+tuple(list(tup)[1:])

def replaceAcros(s):
    names = {"DSYN": "DS", "DPT": "DP", "DHYP": "DH", "GSYN": "GS", "GDES": "GD"}
    replaced = s
    for key in names.keys():
        replaced = replaced.replace(key, names[key])
    return replaced