package org.openintents.distribution;

public interface LicensedApplication {
    boolean isLicenseValid();

    void newLicense();
}
