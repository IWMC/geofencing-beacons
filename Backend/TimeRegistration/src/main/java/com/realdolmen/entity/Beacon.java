package com.realdolmen.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;
import java.util.HashSet;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "Beacon.findAll", query = "SELECT DISTINCT b FROM Beacon b LEFT JOIN FETCH b.occupations ORDER BY b.id"),
        @NamedQuery(name = "Beacon.findAllForEmployee", query =
                "SELECT DISTINCT b FROM Beacon b " +
                        "INNER JOIN FETCH b.occupations o " +
                        "WHERE TYPE(o) IN (Occupation) OR :employee MEMBER OF o.employees " +
                        "ORDER BY b.id")
})
public class Beacon implements Serializable, Initializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Version
    @Column(name = "version")
    private int version;


    @OneToMany
    private Set<Occupation> occupations = new HashSet<>();

    @Embedded
    @NotNull(message = "mode.empty")
    private BeaconMode mode;

    public Long getId() {
        return this.id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public int getVersion() {
        return this.version;
    }

    public void setVersion(final int version) {
        this.version = version;
    }

    public Set<Occupation> getOccupations() {
        return this.occupations;
    }

    public void setOccupations(final Set<Occupation> occupations) {
        this.occupations = occupations;
    }

    public BeaconMode getMode() {
        return mode;
    }

    public void setMode(BeaconMode mode) {
        this.mode = mode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Beacon)) {
            return false;
        }
        Beacon other = (Beacon) obj;
        if (id != null) {
            if (!id.equals(other.id)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public void initialize() {
        getOccupations().forEach(Initializable::initialize);
    }
}