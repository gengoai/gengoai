import numpy as np
import sys

embeddings_dir = sys.argv[1]
raw_dir = sys.argv[2]

dimensions = [50, 100, 200, 300]
for dimension in dimensions:
    input_file = f"{raw_dir}/glove.6B.{dimension}d.txt"
    output_file = f"{embeddings_dir}/glove{dimension}.npy"

    print(f"Building {dimension} dimension glove vectors...", end=" ")
    words = ["--PAD--", "--UNKNOWN--"]
    vectors = [np.array([0.0 for i in range(dimension)]), np.random.rand(dimension)]
    with open(input_file) as fp:
        for line in fp:
            line = line.strip().split(" ")
            if len(line) > dimension:
                word = line[0]
                vec = np.array([float(x) for x in line[1:]])
                words.append(word)
                vectors.append(vec)

    matrix = np.zeros(((len(vectors)), dimension))
    for (i, v) in enumerate(vectors):
        matrix[i] = v

    np.save(output_file, matrix)
    print(" [COMPLETED]")
    with open(f"{embeddings_dir}/glove.vocab", "w") as fp:
        for word in words:
            fp.write(word)
            fp.write("\n")
