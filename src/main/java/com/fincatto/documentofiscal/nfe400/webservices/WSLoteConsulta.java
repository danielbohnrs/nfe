package com.fincatto.documentofiscal.nfe400.webservices;

import java.math.BigDecimal;
import java.rmi.RemoteException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.httpclient.protocol.Protocol;

import com.fincatto.documentofiscal.DFLog;
import com.fincatto.documentofiscal.DFModelo;
import com.fincatto.documentofiscal.nfe.NFeConfig;
import com.fincatto.documentofiscal.nfe400.classes.NFAutorizador400;
import com.fincatto.documentofiscal.nfe400.classes.lote.consulta.NFLoteConsulta;
import com.fincatto.documentofiscal.nfe400.classes.lote.consulta.NFLoteConsultaRetorno;
import com.fincatto.documentofiscal.nfe400.webservices.gerado.NFeRetAutorizacao4Stub;
import com.fincatto.documentofiscal.nfe400.webservices.gerado.NFeRetAutorizacao4Stub.NfeResultMsg;

class WSLoteConsulta implements DFLog {
    
    private final NFeConfig config;
    
    WSLoteConsulta(final NFeConfig config) {
        this.config = config;
    }
    
    NFLoteConsultaRetorno consultaLote(final String numeroRecibo, final DFModelo modelo) throws Exception {
        final OMElement omElementConsulta = AXIOMUtil.stringToOM(this.gerarDadosConsulta(numeroRecibo).toString());
        this.getLogger().debug(omElementConsulta.toString());
        
        final OMElement omElementResult = this.efetuaConsulta(omElementConsulta, modelo);
        this.getLogger().debug(omElementResult.toString());
        
        return this.config.getPersister().read(NFLoteConsultaRetorno.class, omElementResult.toString());
    }
    
    private OMElement efetuaConsulta(final OMElement omElement, final DFModelo modelo) throws Exception {
        final NFeRetAutorizacao4Stub.NfeDadosMsg dados = new NFeRetAutorizacao4Stub.NfeDadosMsg();
        dados.setExtraElement(omElement);
        
        final NFAutorizador400 autorizador = NFAutorizador400.valueOfTipoEmissao(this.config.getTipoEmissao(), this.config.getCUF());
        final String urlWebService = DFModelo.NFCE.equals(modelo) ? autorizador.getNfceRetAutorizacao(this.config.getAmbiente()) : autorizador.getNfeRetAutorizacao(this.config.getAmbiente());
        if (urlWebService == null) {
            throw new IllegalArgumentException("Nao foi possivel encontrar URL para RetAutorizacao " + modelo.name() + ", autorizador " + autorizador.name());
        }
        Protocol.registerProtocol("https", config.createProtocol());//DJB-06/06/2022
        final NfeResultMsg autorizacaoLoteResult = new NFeRetAutorizacao4Stub(urlWebService, config).nfeRetAutorizacaoLote(dados);
        Protocol.unregisterProtocol("https");//DJB-06/06/2022
        return autorizacaoLoteResult.getExtraElement();
    }
    
    private NFLoteConsulta gerarDadosConsulta(final String numeroRecibo) {
        final NFLoteConsulta consulta = new NFLoteConsulta();
        consulta.setRecibo(numeroRecibo);
        consulta.setAmbiente(this.config.getAmbiente());
        consulta.setVersao(new BigDecimal(this.config.getVersao()));
        return consulta;
    }
}
