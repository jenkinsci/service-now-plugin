package org.jenkinsci.plugins.servicenow;

import com.datapipe.jenkins.vault.VaultAccessor;
import com.datapipe.jenkins.vault.credentials.VaultCredential;
import org.jenkinsci.plugins.servicenow.model.VaultConfiguration;

import java.util.Map;

public class VaultService {

    public static Map<String, String> readVaultData(VaultConfiguration vaultConfiguration, VaultCredential credentials) {
        VaultAccessor accessor = new VaultAccessor();
        accessor.init(vaultConfiguration.getUrl());
        accessor.auth(credentials);
        return accessor.read(vaultConfiguration.getPath());
    }
}
