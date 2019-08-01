import sys

from gensim.models import Word2Vec
from gensim.models import KeyedVectors

print("Loading model...")
model = KeyedVectors.load_word2vec_format(sys.argv[1], binary=True)
print("Model loaded.")

k = 3

input = sys.stdin.read().splitlines()

for line in input:
    words = line.split(" ")

    # Remove words not in the dictionary
    for word in words:
        if word not in model.vocab:
            words.remove(word)

    if not words:
        continue

    similar = model.most_similar(words, topn=k)
    for result in similar:
        print("{} => {}".format(line, result[0]))