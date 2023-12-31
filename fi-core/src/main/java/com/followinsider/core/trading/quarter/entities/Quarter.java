package com.followinsider.core.trading.quarter.entities;

import com.followinsider.common.BaseEntity;
import com.followinsider.common.SyncStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Table(name = "fiscal_quarter")
public class Quarter extends BaseEntity {

    @Column(nullable = false, name = "year_val")
    private int year;

    @Column(nullable = false, name = "quarter_val")
    private int quarter;

    @Enumerated
    private SyncStatus syncStatus = SyncStatus.PENDING;

    private Integer formNum;

    private int invalidFormNum = 0;

    public Quarter(QuarterVals vals) {
        this.year = vals.year();
        this.quarter = vals.quarter();
    }

    public String getAlias() {
        return getVals().getAlias();
    }

    public QuarterVals getVals() {
        return new QuarterVals(year, quarter);
    }

}
