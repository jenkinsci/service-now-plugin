package org.jenkinsci.plugins.servicenow

import com.datapipe.jenkins.vault.VaultAccessor
import com.datapipe.jenkins.vault.credentials.VaultCredential
import org.jenkinsci.plugins.servicenow.model.VaultConfiguration

fun readVaultData(vaultConfiguration: VaultConfiguration, credentials: VaultCredential): Map<String, String> {
    val accessor = VaultAccessor()
    accessor.init(vaultConfiguration.url)
    accessor.auth(credentials)
    return accessor.read(vaultConfiguration.path).data
}

class ServiceNowPluginException(message: String) : RuntimeException(message)
