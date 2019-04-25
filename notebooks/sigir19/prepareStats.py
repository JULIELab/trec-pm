def prepareStats(path):
    """
    Reads a single stats CSV file, excludes the 'all' row and converts the topic numbers to ints.
    Then sets the Topic columns as the new index.
    Returns a DataFrame indexed by the non-'all' topics.
    """
    import pandas as pd
    df = pd.read_csv(path).query("Topic != 'all'")
    df["Topic"] = df["Topic"].astype(int)
    df.sort_values(by="Topic", inplace=True)
    df = df.set_index("Topic")
    return df