package org.prototype.web;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@javax.persistence.Entity
@Table(name = "ConfigEntry", uniqueConstraints = { @UniqueConstraint(columnNames = "ConfigKey") })
public class ConfigEntry {
    private Long      id;
    private ConfigKey key;
    private String    value;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "Id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "ConfigKey", length = 30)
    public ConfigKey getKey() {
        return key;
    }

    public void setKey(ConfigKey key) {
        this.key = key;
    }

    @Column(name = "ConfigValue", length = 100)
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
