package com.example;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Cat extends Pet {
  @JsonProperty("huntingSkill")
  HuntingSkillEnum huntingSkill;

  public HuntingSkillEnum getHuntingSkill() {
    return huntingSkill;
  }

  public void setHuntingSkill(HuntingSkillEnum huntingSkill) {
    this.huntingSkill = huntingSkill;
  }

  public enum HuntingSkillEnum {
    clueless,

    lazy,

    adventurous,

    aggressive
  }
}
