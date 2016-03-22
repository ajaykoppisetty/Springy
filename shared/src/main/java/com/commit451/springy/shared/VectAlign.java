package com.commit451.springy.shared;


import com.commit451.springy.shared.techniques.AbstractFillMode;
import com.commit451.springy.shared.techniques.BaseFillMode;
import com.commit451.springy.shared.techniques.NWAlignment;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class used to align VectorDrawable sequences in order to allow morphing animations between them.
 */
public final class VectAlign {

  private static final int MAX_ALIGN_ITERATIONS = 5;

  /**
   * Alignment technique
   */
  public enum Mode {
    /**
     * Inject necessary elements by repeating existing ones
     */
    BASE(1);

    //TODO more technique

    private int mode;

    Mode(int mode) {
      this.mode = mode;
    }
  }

  /**
   * @param from The source path represented in a String
   * @param to The target path represented in a String
   * @return whether the <code>from</code> can morph into <code>to</code>
   */
  public static boolean canMorph(String from, String to) {
    return PathParser.canMorph(PathParser.createNodesFromPathData(from),
        PathParser.createNodesFromPathData(to));
  }

  /**
   * Align two VectorDrawable sequences in order to make them <i>morphable</i>
   */
  public static String[] align(String from, String to, Mode alignMode) {
    String result[] = null;

    //read data as nodes
    PathParser.PathDataNode[] fromList = PathParser.createNodesFromPathData(from);
    PathParser.PathDataNode[] toList = PathParser.createNodesFromPathData(to);


    if (PathParser.canMorph(fromList, toList)) {
      result = new String[] { from, to };
    } else {

      //Aligning
      boolean equivalent = false;
      int extraCloneNodes = 0;
      ArrayList<PathParser.PathDataNode> alignedFrom = null;
      ArrayList<PathParser.PathDataNode> alignedTo = null;

      for (int i = 0; i < MAX_ALIGN_ITERATIONS && !equivalent; i++) {
        NWAlignment nw = new NWAlignment(PathNodeUtils.transform(fromList, extraCloneNodes, true),
            PathNodeUtils.transform(toList, extraCloneNodes, true));
        nw.align();
        alignedFrom = nw.getAlignedFrom();
        alignedTo = nw.getAlignedTo();
        equivalent = PathNodeUtils.isEquivalent(nw.getOriginalFrom(), nw.getAlignedFrom())
            && PathNodeUtils.isEquivalent(nw.getOriginalTo(), nw.getAlignedTo());

        extraCloneNodes++;
      }

      if (!equivalent) {
        return null;
      }

      AbstractFillMode fillMode = null;
      switch (alignMode) {
        case BASE:
          fillMode = new BaseFillMode();
          break;

        //TODO handle more cases here
      }

      //Fill
      fillMode.fillInjectedNodes(alignedFrom, alignedTo);

      //Simplify
      PathNodeUtils.simplify(alignedFrom, alignedTo);

      result = new String[] {
          PathNodeUtils.pathNodesToString(alignedFrom), PathNodeUtils.pathNodesToString(alignedTo)
      };
    }
    return result;
  }

  // ###### DEBUG ######
  // ###################

  static void printPathData(PathParser.PathDataNode[] data) {
    int i = 1;
    for (PathParser.PathDataNode node : data) {
      System.out.println((i++) + ".\t" + node.type + " " + Arrays.toString(node.params));
    }
  }

  static void printPathData(ArrayList<PathParser.PathDataNode> data) {
    int i = 1;
    for (PathParser.PathDataNode node : data) {
      System.out.println((i++) + ".\t" + node.type + " " + Arrays.toString(node.params));
    }
  }
}
