package com.example;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Cat extends Pet {
    @JsonProperty("huntingSkill")
    private HuntingSkillEnum huntingSkill = HuntingSkillEnum.LAZY;

    public HuntingSkillEnum getHuntingSkill() {
        return huntingSkill;
    }

    public void setHuntingSkill(HuntingSkillEnum huntingSkill) {
        this.huntingSkill = huntingSkill;
    }

    public enum HuntingSkillEnum {
        @JsonProperty("clueless")
        CLUELESS,

        @JsonProperty("lazy")
        LAZY,

        @JsonProperty("adventurous")
        ADVENTUROUS,

        @JsonProperty("aggressive")
        AGGRESSIVE
    }
}
