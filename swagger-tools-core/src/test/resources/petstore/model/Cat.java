package com.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
