def getBaselineDf():
	import pandas as pd
	df17 = pd.read_csv("/Users/faessler/nfshome/Research/sigir/recall/recall_analysis2017.csv", index_col="run").round(2)
	df18 = pd.read_csv("/Users/faessler/nfshome/Research/sigir/recall/recall_analysis2018.csv", index_col="run").round(2)
	df17["year"] = pd.Series([2017]*len(df17), index=df17.index)
	df18["year"] = pd.Series([2018]*len(df18), index=df18.index)


	# # Baseline runs
	# bl = baseline:
	# - best_fields
	# - OR operator
	# - no word removal

	# In[21]:


	bl17 = df17.query("multfields == 'best_fields' and op == 'OR' and wordremoval == False")
	bl18 = df18.query("multfields == 'best_fields' and op == 'OR' and wordremoval == False")


	# In[22]:


	df = pd.concat([bl17,bl18])


	# In[23]:


	bltable = df[["recall","infNDCG", "Rprec", "P_10", "mean_r", "std_r","year"]].loc["NONE"].reset_index().drop("run",axis=1)

	return bltable