package de.gameplayjdk.jwfc;

import de.gameplayjdk.jwfc.test.Tile;
import de.gameplayjdk.jwfc.test.TileInterface;
import de.gameplayjdk.jwfc.test.TileMap;

public class Main {

    public static void main(String[] args) {
        (new Main())
                .test();
    }

    private void test() {
        int[] mapRaw = {
                0, 0, 0, 0, 0,
                0, 1, 1, 1, 0,
                0, 1, 2, 1, 0,
                0, 1, 1, 1, 0,
                0, 0, 0, 0, 0,
        };

        int width = 5;
        int height = 5;

        Tile[] tileMap = new Tile[mapRaw.length];
        for (int index = 0; index < mapRaw.length; index++) {
            tileMap[index] = new Tile(mapRaw[index]);
        }

        this.printTileMap(width, height, tileMap);

        System.out.println();

        TileMap t = new TileMap(width, height, tileMap);
        t.analyze();

        int widthNew = width * 2;
        int heightNew = height * 2;

        TileInterface[] tileMapNew = t.generate(widthNew, heightNew, true);

        System.out.println();

        this.printTileMap(widthNew, heightNew, tileMapNew);

        // TODO: Generic mapping of tileMapNew to the correct type (Tile).
    }

    private void printTileMap(int width, int height, TileInterface[] tileMap) {
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                System.out.print(tileMap[(h * width) + w].getId());
                System.out.print(' ');
            }
            System.out.println();
        }
    }
}
