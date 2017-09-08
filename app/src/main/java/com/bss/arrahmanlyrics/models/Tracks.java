package com.bss.arrahmanlyrics.models;

import com.thoughtbot.expandablecheckrecyclerview.models.SingleCheckExpandableGroup;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class Tracks extends SingleCheckExpandableGroup{

  public Tracks(String title, List<albumsongs> items) {
    super(title, items);
  }
}