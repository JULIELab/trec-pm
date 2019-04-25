def addMidruleToLatex(latex):
	# from https://stackoverflow.com/questions/32275070/midrule-in-latex-output-of-python-pandas
	# split lines into a list
	latex_list = latex.splitlines()

	# insert a `\midrule` at third last position in list (which will be the fourth last line in latex output)
	latex_list.insert(len(latex_list)-3, '\midrule')

	# join split lines to get the modified latex output string
	latex_new = '\n'.join(latex_list)
	print(latex_new)