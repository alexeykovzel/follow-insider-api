package com.followinsider.core.trading.form.failed;

import com.followinsider.common.BaseEntity;
import com.followinsider.secapi.forms.refs.FormRef;
import com.followinsider.secapi.forms.FormType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "form_failed")
public class FailedForm extends BaseEntity {

    @Column(length = 20)
    private String accNum;

    @Column(nullable = false, length = 10)
    private String issuerCik;

    @Column(nullable = false)
    private LocalDate filedAt;

    @Column(nullable = false)
    private String error;

    public FailedForm(FormRef ref, String error) {
        this.accNum = ref.accNum();
        this.issuerCik = ref.issuerCik();
        this.filedAt = ref.filedAt();
        this.error = error;
    }

    public FormRef getRef() {
        return new FormRef(accNum, issuerCik, FormType.F4, filedAt);
    }

}
