package com.fincatto.documentofiscal.nfe400.webservices;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.httpclient.protocol.Protocol;

import com.fincatto.documentofiscal.DFLog;
import com.fincatto.documentofiscal.DFUnidadeFederativa;
import com.fincatto.documentofiscal.nfe.NFeConfig;
import com.fincatto.documentofiscal.nfe400.NotaFiscalChaveParser;
import com.fincatto.documentofiscal.nfe400.classes.NFAutorizador400;
import com.fincatto.documentofiscal.nfe400.classes.evento.NFEnviaEventoRetorno;
import com.fincatto.documentofiscal.nfe400.classes.evento.manifestacaodestinatario.NFEnviaEventoManifestacaoDestinatario;
import com.fincatto.documentofiscal.nfe400.classes.evento.manifestacaodestinatario.NFEventoManifestacaoDestinatario;
import com.fincatto.documentofiscal.nfe400.classes.evento.manifestacaodestinatario.NFInfoEventoManifestacaoDestinatario;
import com.fincatto.documentofiscal.nfe400.classes.evento.manifestacaodestinatario.NFInfoManifestacaoDestinatario;
import com.fincatto.documentofiscal.nfe400.classes.evento.manifestacaodestinatario.NFProtocoloEventoManifestacaoDestinatario;
import com.fincatto.documentofiscal.nfe400.classes.evento.manifestacaodestinatario.NFTipoEventoManifestacaoDestinatario;
import com.fincatto.documentofiscal.nfe400.webservices.gerado.NFeRecepcaoEvento4Stub;
import com.fincatto.documentofiscal.nfe400.webservices.gerado.NFeRecepcaoEvento4Stub.NfeResultMsg;
import com.fincatto.documentofiscal.utils.DFAssinaturaDigital;

public class WSManifestacaoDestinatario implements DFLog {
    
    private static final BigDecimal VERSAO_LEIAUTE = new BigDecimal("1.00");
    private final NFeConfig config;
    
    public WSManifestacaoDestinatario(final NFeConfig config) {
        this.config = config;
    }
    
    NFEnviaEventoRetorno manifestaDestinatarioNotaAssinada(final String chaveAcesso, final String eventoAssinadoXml) throws Exception {
        final OMElement omElementResult = this.efetuaManifestacaoDestinatario(eventoAssinadoXml, chaveAcesso);
        return this.config.getPersister().read(NFEnviaEventoRetorno.class, omElementResult.toString());
    }
    
    NFEnviaEventoRetorno manifestaDestinatarioNota(final String chaveAcesso, final NFTipoEventoManifestacaoDestinatario tipoEvento, final String motivo, final String cnpj) throws Exception {
        final String manifestacaoDestinatarioNotaXML = this.gerarDadosManifestacaoDestinatario(chaveAcesso, tipoEvento, motivo, cnpj).toString();
        final String xmlAssinado = new DFAssinaturaDigital(this.config).assinarDocumento(manifestacaoDestinatarioNotaXML);
        final OMElement omElementResult = this.efetuaManifestacaoDestinatario(xmlAssinado, chaveAcesso);
        return this.config.getPersister().read(NFEnviaEventoRetorno.class, omElementResult.toString());
    }
    
    NFProtocoloEventoManifestacaoDestinatario manifestaDestinatarioNotaProtocolo(final String chaveAcesso, final NFTipoEventoManifestacaoDestinatario tipoEvento, final String motivo, final String cnpj) throws Exception {
        final String manifestacaoDestinatarioNotaXML = this.gerarDadosManifestacaoDestinatario(chaveAcesso, tipoEvento, motivo, cnpj).toString();
    
        final String xmlAssinado = new DFAssinaturaDigital(this.config).assinarDocumento(manifestacaoDestinatarioNotaXML);
        final OMElement omElementResult = this.efetuaManifestacaoDestinatario(xmlAssinado, chaveAcesso);
    
        final NFEnviaEventoManifestacaoDestinatario evento = this.config.getPersister().read(NFEnviaEventoManifestacaoDestinatario.class, xmlAssinado);
        final NFEnviaEventoRetorno retorno = this.config.getPersister().read(NFEnviaEventoRetorno.class, omElementResult.toString());
        
        // Excessao se o codigo status do retorno diferente de 128 - Lote de Evento Processado
        if (retorno.getCodigoStatusReposta() != 128) {
            throw new RuntimeException("Status: " + retorno.getCodigoStatusReposta() + " - Motivo: " + retorno.getMotivo());
        }
        
        NFProtocoloEventoManifestacaoDestinatario nfProtocoloEventoManifestacaoDestinatario = new NFProtocoloEventoManifestacaoDestinatario();
        nfProtocoloEventoManifestacaoDestinatario.setVersao(evento.getVersao());
        nfProtocoloEventoManifestacaoDestinatario.setEvento(evento.getEvento().get(0));
        nfProtocoloEventoManifestacaoDestinatario.setEventoRetorno(retorno.getEventoRetorno().get(0));
        return nfProtocoloEventoManifestacaoDestinatario;
    }
    
    private OMElement efetuaManifestacaoDestinatario(final String xmlAssinado, final String chaveAcesso) throws Exception {
        final NFeRecepcaoEvento4Stub.NfeDadosMsg dados = new NFeRecepcaoEvento4Stub.NfeDadosMsg();
        final OMElement omElementXML = AXIOMUtil.stringToOM(xmlAssinado);
        this.getLogger().debug(omElementXML.toString());
        dados.setExtraElement(omElementXML);
    
        final NotaFiscalChaveParser parser = new NotaFiscalChaveParser(chaveAcesso);
        final NFAutorizador400 autorizador = NFAutorizador400.valueOfChaveAcesso(chaveAcesso);
        final String urlWebService = autorizador.getRecepcaoEventoAN(this.config.getAmbiente());
        if (urlWebService == null) {
            throw new IllegalArgumentException("Nao foi possivel encontrar URL para RecepcaoEvento " + parser.getModelo().name() + ", autorizador " + autorizador.name());
        }
    
        Protocol.registerProtocol("https", config.createProtocol());//DJB-06/06/2022
        final NfeResultMsg nfeRecepcaoEvento = new NFeRecepcaoEvento4Stub(urlWebService, config).nfeRecepcaoEvento(dados);
        Protocol.unregisterProtocol("https");//DJB-06/06/2022
        final OMElement omElementResult = nfeRecepcaoEvento.getExtraElement();
        this.getLogger().debug(omElementResult.toString());
        return omElementResult;
    }
    
    private NFEnviaEventoManifestacaoDestinatario gerarDadosManifestacaoDestinatario(final String chaveAcesso, final NFTipoEventoManifestacaoDestinatario tipoEvento, final String motivo, final String cpfOuCnpj) {
        final NFInfoManifestacaoDestinatario manifestacaoDestinatario = new NFInfoManifestacaoDestinatario();
        manifestacaoDestinatario.setDescricaoEvento(tipoEvento.getDescricao());
        manifestacaoDestinatario.setVersao(WSManifestacaoDestinatario.VERSAO_LEIAUTE);
        manifestacaoDestinatario.setJustificativa(motivo);
        
        final NFInfoEventoManifestacaoDestinatario infoEvento = new NFInfoEventoManifestacaoDestinatario();
        infoEvento.setAmbiente(this.config.getAmbiente());
        infoEvento.setChave(chaveAcesso);
        if (cpfOuCnpj.length() == 11) {
            infoEvento.setCpf(cpfOuCnpj);
        } else {
            infoEvento.setCnpj(cpfOuCnpj);
        }
        infoEvento.setDataHoraEvento(ZonedDateTime.now(this.config.getTimeZone().toZoneId()));
        infoEvento.setId(String.format("ID%s%s0%s", tipoEvento.getCodigo(), chaveAcesso, "1"));
        infoEvento.setNumeroSequencialEvento(1);
        infoEvento.setOrgao(DFUnidadeFederativa.RFB);
        infoEvento.setCodigoEvento(tipoEvento.getCodigo());
        infoEvento.setVersaoEvento(WSManifestacaoDestinatario.VERSAO_LEIAUTE);
        infoEvento.setManifestacaoDestinatario(manifestacaoDestinatario);
        
        final NFEventoManifestacaoDestinatario evento = new NFEventoManifestacaoDestinatario();
        evento.setInfoEvento(infoEvento);
        evento.setVersao(WSManifestacaoDestinatario.VERSAO_LEIAUTE);
        
        final NFEnviaEventoManifestacaoDestinatario enviaEvento = new NFEnviaEventoManifestacaoDestinatario();
        enviaEvento.setEvento(Collections.singletonList(evento));
        enviaEvento.setIdLote(Long.toString(ZonedDateTime.now(this.config.getTimeZone().toZoneId()).toInstant().toEpochMilli()));
        enviaEvento.setVersao(WSManifestacaoDestinatario.VERSAO_LEIAUTE);
        return enviaEvento;
    }
}
