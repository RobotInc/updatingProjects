package com.bss.arrahmanlyrics.models;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class Tracks extends ExpandableGroup<albumsongs> {

  public Tracks(String title, List<albumsongs> items) {
    super(title, items);
  }
}