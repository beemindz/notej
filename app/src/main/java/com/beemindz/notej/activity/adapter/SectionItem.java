package com.beemindz.notej.activity.adapter;

/**
 * Created by vanch_000 on 8/8/2014.
 */
public class SectionItem implements Item {

  private String title;

  public SectionItem(String title) {
    this.title = title;
  }

  public String getTitle(){
    return title;
  }

  @Override
  public boolean isSection() {
    return true;
  }
}
