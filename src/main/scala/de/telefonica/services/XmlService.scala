package de.telefonica.services

import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives
import de.telefonica.repo.HBaseRepository


/**
  * Created by denniskleine on 22.05.17.
  */
trait XmlService extends Directives with ScalaXmlSupport {

  val logger: LoggingAdapter
  val repo = new HBaseRepository


  lazy val wsdl: String =
    """<?xml version="1.0" ?>
      <wsdl:definitions xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/" xmlns:tns="http://model.soap.rtcc.o2.de" xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/" xmlns:ns1="http://schemas.xmlsoap.org/soap/http" name="RTCCService" targetNamespace="http://model.soap.rtcc.o2.de">
        <wsdl:types>
          <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:tns="http://model.soap.rtcc.o2.de" attributeFormDefault="unqualified" elementFormDefault="unqualified" targetNamespace="http://model.soap.rtcc.o2.de">
            <xs:complexType name="getOfferByIdRequest">
              <xs:sequence>
                <xs:element minOccurs="0" name="offerId" type="xs:int"></xs:element>
              </xs:sequence>
            </xs:complexType>
            <xs:complexType name="getOfferByIdResponseMsg">
              <xs:sequence>
                <xs:element minOccurs="0" name="channelId" type="xs:int"></xs:element>
                <xs:element minOccurs="0" name="channelName" type="xs:string"></xs:element>
                <xs:element minOccurs="0" name="offerDescription" type="xs:string"></xs:element>
                <xs:element minOccurs="0" name="price" type="xs:double"></xs:element>
                <xs:element minOccurs="0" name="validFrom" type="xs:dateTime"></xs:element>
                <xs:element minOccurs="0" name="validTo" type="xs:dateTime"></xs:element>
              </xs:sequence>
            </xs:complexType>
          </xs:schema>
        </wsdl:types>
        <wsdl:message name="getOfferById">
          <wsdl:part name="getOfferByIdRequest" type="tns:getOfferByIdRequest">
          </wsdl:part>
        </wsdl:message>run
        <wsdl:message name="getOfferByIdResponse">
          <wsdl:part name="getOfferByIdResponseMsg" type="tns:getOfferByIdResponseMsg">
          </wsdl:part>
        </wsdl:message>
        <wsdl:portType name="RTCCService">
          <wsdl:operation name="getOfferById">
            <wsdl:input message="tns:getOfferById" name="getOfferById">
            </wsdl:input>
            <wsdl:output message="tns:getOfferByIdResponse" name="getOfferByIdResponse">
            </wsdl:output>
          </wsdl:operation>
        </wsdl:portType>
        <wsdl:binding name="RTCCServiceSoapBinding" type="tns:RTCCService">
          <soap:binding style="rpc" transport="http://schemas.xmlsoap.org/soap/http"></soap:binding>
          <wsdl:operation name="getOfferById">
            <soap:operation soapAction="" style="rpc"></soap:operation>
            <wsdl:input name="getOfferById">
              <soap:body namespace="http://model.soap.rtcc.o2.de" use="literal"></soap:body>
            </wsdl:input>
            <wsdl:output name="getOfferByIdResponse">
              <soap:body namespace="http://model.soap.rtcc.o2.de" use="literal"></soap:body>
            </wsdl:output>
          </wsdl:operation>
        </wsdl:binding>
        <wsdl:service name="RTCCService">
          <wsdl:port binding="tns:RTCCServiceSoapBinding" name="RTCCServicePort">
            <soap:address location="http://delxvi40.de.pri.o2.com:8280/RTCC"></soap:address>
          </wsdl:port>
        </wsdl:service>
      </wsdl:definitions>"""

  lazy val response =
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:mod="http://model.soap.rtcc.o2.de">
      <soapenv:Header/>
      <soapenv:Body>
        <mod:getOfferByIdResponse>
          <getOfferByIdResponseMsg>
            <!--Optional:-->
            <channelId>10</channelId>
            <!--Optional:-->
            <channelName>myChannel</channelName>
            <!--Optional:-->
            <offerDescription>This is a test</offerDescription>
            <!--Optional:-->
            <price>11.12</price>
            <!--Optional:-->
            <validFrom>?</validFrom>
            <!--Optional:-->
            <validTo>?</validTo>
          </getOfferByIdResponseMsg>
        </mod:getOfferByIdResponse>
      </soapenv:Body>
    </soapenv:Envelope>


  /**
      * Reference: https://hbase.apache.org/book.html#_rest
      *
      * Cluster-Wide Endpoints
      * GET /api/v1/xml                 - Test: Static XML Code
      * GET /api/v1/wsdl                - Test: Static WSDL Code
      *
      * */
    val route =
      pathPrefix("api" / "v1") {
        get {
          path("ping") {
            logger.info("Simple XML rest ping")
            complete("XML Service is working (PONG!)")
          } ~
          path("wsdl") {
            logger.info("Response WSDL structure for SAS RTDM")
            complete(HttpEntity(ContentTypes.`text/xml(UTF-8)`, wsdl))
          } ~
          path("rtcc") {
            logger.info("Response XML for SAS RDTM")
            complete(response)
          }
        }
      }
}
