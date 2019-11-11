# jwfc (java-wfc)

An abstract java implementation of the wafe function collapse (wfc) algorithm.

It is specifically designed with the simple tiled version of the algorithm in mind. 

Currently the project is work in progress and more of a proof of concept type of thing.

**Example**:

_(This assumes an IntelliJ IDEA artifact build was configured and used to generate a jar file in the default location)_.
Feel free to experiment with different map dimensions and types inside the `TileMapTest` class.

```bash
java -jar out/artifacts/java_wfc_jar/java-wfc.jar
:: input (width=5, height=5)
0  0  0  0  0
0  1  1  1  0
0  1  2  1  0
0  1  1  1  0
0  0  0  0  0
:: output (width=10, height=10, periodic=false)
0  1  1  1  0  1  1  2  1  2
0  0  0  0  1  1  0  1  2  1
1  1  1  1  1  2  1  2  1  1
1  1  2  1  2  1  0  1  1  0
0  1  1  2  1  0  1  2  1  1
0  1  2  1  0  1  1  1  1  1
0  1  1  2  1  1  0  0  0  0
0  1  1  1  0  1  1  0  1  0
0  1  2  1  0  0  1  0  1  0
1  2  1  0  1  0  0  0  1  1
:: output (width=10, height=10, periodic=true)
0  0  0  0  1  1  0  0  0  1
0  1  1  0  0  1  1  1  0  1
1  1  2  1  0  0  1  1  0  1
1  1  1  0  0  0  0  0  0  0
2  1  0  1  0  1  0  1  0  1
1  0  1  0  1  1  0  1  1  1
0  0  1  0  0  1  1  1  1  0
0  0  1  1  1  2  1  2  1  1
0  0  0  0  1  1  2  1  0  1
0  1  1  1  1  1  1  1  1  0

```

You can find the original algorithm [here](https://github.com/mxgmn/WaveFunctionCollapse) (written in C#).
