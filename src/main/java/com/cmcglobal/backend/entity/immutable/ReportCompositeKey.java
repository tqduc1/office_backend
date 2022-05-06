package com.cmcglobal.backend.entity.immutable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportCompositeKey implements Serializable {
    @Column(name = "owner")
    String owner;

    @Column(name = "floor_id")
    Integer floorId;
}
