package com.madebyjoaogomes.portfolio.site;

/** The site deploy could not be requested: either it is not configured or the hook failed. */
public class SiteDeployException extends RuntimeException {

    private final boolean configured;

    private SiteDeployException(String message, boolean configured, Throwable cause) {
        super(message, cause);
        this.configured = configured;
    }

    static SiteDeployException notConfigured() {
        return new SiteDeployException("Defina SITE_DEPLOY_HOOK_URL para atualizar o site pelo painel", false, null);
    }

    static SiteDeployException hookFailed(Throwable cause) {
        return new SiteDeployException("O Deploy Hook do site não aceitou o pedido; tente de novo em instantes", true, cause);
    }

    public boolean isConfigured() {
        return configured;
    }
}
