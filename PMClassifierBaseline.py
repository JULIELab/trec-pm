
# coding: utf-8

# In[17]:


from sklearn.datasets import load_svmlight_file
from sklearn.linear_model import LogisticRegression
from sklearn.svm import SVC
from sklearn.model_selection import cross_val_score
from sklearn.model_selection import GridSearchCV
import numpy as np


# In[18]:


# This is a tuple of a sparse matrix and a label vector. Labels in SVM light format are 1 (PM) and -1 (Not PM)
data = load_svmlight_file("features.svmlight.gz")


# In[19]:


clf = LogisticRegression()
scores = cross_val_score(clf, data[0], data[1], cv=10, n_jobs=24, scoring='accuracy')
print("Crossval scores:", scores)
# Should be around 0.75
print("Mean score:", np.mean(scores))


# In[ ]:


param_grid = {
    "kernel":["rbf","poly"], 
    "C":[.25,.5,1,2,4,100], 
    "gamma":[0.17, 2, 4],
    "coef0":[0.8, 1.5, 3],
    "degree":[2]}
svm = GridSearchCV(SVC(), param_grid, cv=10,n_jobs=24)
svm.fit(data[0], data[1])
print("Best parameters set found on development set:")
print()
print(clf.best_params_)
print()
print("With score: ", clf.best_score_)
print()
print("Grid scores on development set:")
print()
means = clf.cv_results_['mean_test_score']
stds = clf.cv_results_['std_test_score']
for mean, std, params in zip(means, stds, clf.cv_results_['params']):
    print("%0.3f (+/-%0.03f) for %r"
          % (mean, std * 2, params))
print()

print("Detailed classification report:")
print()
print("The model is trained on the full development set.")
print("The scores are computed on the full evaluation set.")
print()
y_true, y_pred = y, clf.predict(X)
print(classification_report(y_true, y_pred))
print()
print("ACC:",accuracy_score(y_true,y_pred))

