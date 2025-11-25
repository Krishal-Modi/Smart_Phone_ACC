package com.example.SmartPhone.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "data")
public class Phone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;
    private String model;

    @Column(name = "price_cad")
    private Double priceCAD;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "image_url")
    private String imageUrl;

    // additional spec fields (map to columns created during import)
    private String processor;
    private String ram;
    private String storage;

    @Column(name = "display_size")
    private String displaySize;

    @Column(name = "display_resolution")
    private String displayResolution;

    @Column(name = "refresh_rate")
    private String refreshRate;

    @Column(name = "camera_mp")
    private String cameraMP;

    @Column(name = "camera_lenses")
    private String cameraLenses;

    @Column(name = "camera_features")
    private String cameraFeatures;

    @Column(name = "battery_capacity")
    private String batteryCapacity;

    @Column(name = "fast_charging")
    private String fastCharging;

    @Column(name = "wireless_charging")
    private String wirelessCharging;

    private String audio;

    private String connectivity;

    @Column(name = "build_quality")
    private String buildQuality;

    private String os;

    @Column(name = "special_features")
    private String specialFeatures;

    public Phone() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Double getPriceCAD() { return priceCAD; }
    public void setPriceCAD(Double priceCAD) { this.priceCAD = priceCAD; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getProcessor() { return processor; }
    public void setProcessor(String processor) { this.processor = processor; }
    public String getRam() { return ram; }
    public void setRam(String ram) { this.ram = ram; }
    public String getStorage() { return storage; }
    public void setStorage(String storage) { this.storage = storage; }
    public String getDisplaySize() { return displaySize; }
    public void setDisplaySize(String displaySize) { this.displaySize = displaySize; }
    public String getDisplayResolution() { return displayResolution; }
    public void setDisplayResolution(String displayResolution) { this.displayResolution = displayResolution; }
    public String getRefreshRate() { return refreshRate; }
    public void setRefreshRate(String refreshRate) { this.refreshRate = refreshRate; }
    public String getCameraMP() { return cameraMP; }
    public void setCameraMP(String cameraMP) { this.cameraMP = cameraMP; }
    public String getCameraLenses() { return cameraLenses; }
    public void setCameraLenses(String cameraLenses) { this.cameraLenses = cameraLenses; }
    public String getCameraFeatures() { return cameraFeatures; }
    public void setCameraFeatures(String cameraFeatures) { this.cameraFeatures = cameraFeatures; }
    public String getBatteryCapacity() { return batteryCapacity; }
    public void setBatteryCapacity(String batteryCapacity) { this.batteryCapacity = batteryCapacity; }
    public String getFastCharging() { return fastCharging; }
    public void setFastCharging(String fastCharging) { this.fastCharging = fastCharging; }
    public String getWirelessCharging() { return wirelessCharging; }
    public void setWirelessCharging(String wirelessCharging) { this.wirelessCharging = wirelessCharging; }
    public String getAudio() { return audio; }
    public void setAudio(String audio) { this.audio = audio; }
    public String getConnectivity() { return connectivity; }
    public void setConnectivity(String connectivity) { this.connectivity = connectivity; }
    public String getBuildQuality() { return buildQuality; }
    public void setBuildQuality(String buildQuality) { this.buildQuality = buildQuality; }
    public String getOs() { return os; }
    public void setOs(String os) { this.os = os; }
    public String getSpecialFeatures() { return specialFeatures; }
    public void setSpecialFeatures(String specialFeatures) { this.specialFeatures = specialFeatures; }
}
