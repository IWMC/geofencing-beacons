package com.realdolmen.entity;

import javax.persistence.Entity;
import java.io.Serializable;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import javax.persistence.Version;
import java.util.Set;
import java.util.HashSet;
import javax.persistence.OneToMany;
import com.realdolmen.entity.BeaconMode;
import javax.persistence.Embedded;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
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
	private Set<Occupation> occupation = new HashSet<>();

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

	public Set<Occupation> getOccupation() {
		return this.occupation;
	}

	public void setOccupation(final Set<Occupation> occupation) {
		this.occupation = occupation;
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
		getOccupation().forEach(Initializable::initialize);
	}
}