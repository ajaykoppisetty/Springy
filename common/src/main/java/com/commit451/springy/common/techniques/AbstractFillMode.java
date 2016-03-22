package com.commit451.springy.common.techniques;

import com.commit451.springy.common.PathParser;

import java.util.ArrayList;

/**
 * Created by ziby on 07/08/15.
 */
public abstract class AbstractFillMode {

  public abstract void fillInjectedNodes(ArrayList<PathParser.PathDataNode> from,
      ArrayList<PathParser.PathDataNode> to);
}
