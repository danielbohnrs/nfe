package com.fincatto.documentofiscal.nfe;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import org.apache.commons.httpclient.protocol.Protocol;

import com.fincatto.documentofiscal.DFConfig;
import com.fincatto.documentofiscal.utils.DFSocketFactory;

/**
 * Configuração basica do sistema de notas fiscais.
 */
public abstract class NFeConfig extends DFConfig {

    public static final String NAMESPACE = "http://www.portalfiscal.inf.br/nfe";

    /**
     * ID de contribuinte, somente para NFCe.
     * @return ID do contribuinte.
     */
    public Integer getCodigoSegurancaContribuinteID() {
        return null;
    }

    /**
     * Codigo de seguranca do contribuinte, com 36 caracteres, somente para NFCe.
     * @return Codigo de seguranca do contribuinte.
     */
    public String getCodigoSegurancaContribuinte() {
        return null;
    }

    /**
     * Tipo da emissao das notas (se normal ou em contingencia).
     * @return Tipo da emissao das notas.
     */
    public NFTipoEmissao getTipoEmissao() {
        return NFTipoEmissao.EMISSAO_NORMAL;
    }

    /**
     * Retorna a versão do XML na receita.
     * @return versão nota fiscal
     */
    public String getVersao() {
        return "4.00";
    }
    
    /*
     * para poder ser sobrescrito na classe que extende
     */
    public Protocol createProtocol() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return new Protocol("https", new DFSocketFactory(this), 443);
    }
}
