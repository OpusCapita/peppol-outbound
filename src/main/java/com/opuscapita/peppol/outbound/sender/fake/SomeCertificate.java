package com.opuscapita.peppol.outbound.sender.fake;

import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.util.Date;
import java.util.Set;

/**
 * A dummy certificate that has all necessary fields for testing purposes
 */
public class SomeCertificate extends X509Certificate {

    SomeCertificate() {
        super();
    }

    @Override
    public X500Principal getSubjectX500Principal() {
        return new X500Principal("C=FI, O=OpusCapita Solutions Oy, OU=PEPPOL FAKE AP, CN=PNOXXXXXX");
    }

    @Override
    public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {

    }

    @Override
    public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {

    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public BigInteger getSerialNumber() {
        return null;
    }

    @Override
    public Principal getIssuerDN() {
        return null;
    }

    @Override
    public Principal getSubjectDN() {
        return null;
    }

    @Override
    public Date getNotBefore() {
        return null;
    }

    @Override
    public Date getNotAfter() {
        return null;
    }

    @Override
    public byte[] getTBSCertificate() throws CertificateEncodingException {
        return new byte[0];
    }

    @Override
    public byte[] getSignature() {
        return new byte[0];
    }

    @Override
    public String getSigAlgName() {
        return null;
    }

    @Override
    public String getSigAlgOID() {
        return null;
    }

    @Override
    public byte[] getSigAlgParams() {
        return new byte[0];
    }

    @Override
    public boolean[] getIssuerUniqueID() {
        return new boolean[0];
    }

    @Override
    public boolean[] getSubjectUniqueID() {
        return new boolean[0];
    }

    @Override
    public boolean[] getKeyUsage() {
        return new boolean[0];
    }

    @Override
    public int getBasicConstraints() {
        return 0;
    }

    @Override
    public byte[] getEncoded() throws CertificateEncodingException {
        return new byte[0];
    }

    @Override
    public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {

    }

    @Override
    public void verify(PublicKey key, String sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {

    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public PublicKey getPublicKey() {
        return null;
    }

    @Override
    public boolean hasUnsupportedCriticalExtension() {
        return false;
    }

    @Override
    public Set<String> getCriticalExtensionOIDs() {
        return null;
    }

    @Override
    public Set<String> getNonCriticalExtensionOIDs() {
        return null;
    }

    @Override
    public byte[] getExtensionValue(String oid) {
        return new byte[0];
    }
}
