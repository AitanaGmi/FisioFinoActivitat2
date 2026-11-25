package com.fisiofino;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fisiofino.model.MalaltiaPacient;
import com.fisiofino.model.Pacient;

/**
 * Llegeix un XML amb DOM i en mostra el contingut sense conèixer-ne l'estructura.
 *
 * Important: amb DOM, builder.parse() llegeix TOT el fitxer i construeix l'arbre
 * sencer en memòria. Només després el recorrem i el mostrem.
 */
public class LectorXML {
    static Pacient p;

    static MalaltiaPacient mp;
    static String medicacio;
    static String dosi;
    static String unitat;
    static String frequencia;
    static String tractament;

    public static List<Pacient> llegirPacients(Path entradaXML) throws IOException, ParserConfigurationException, SAXException {
            Document doc = obtenirDocument(entradaXML);
            Element arrel = doc.getDocumentElement();
            List<Pacient> pacients = new ArrayList<>();

            // Comencem el recorregut recursiu per l'arrel, al nivell 0
            recorrer(arrel, 0,pacients,new ArrayList<>());
            return pacients;
    }

    public static List<MalaltiaPacient> llegirMalalties(Path entradaXML) throws IOException, ParserConfigurationException, SAXException {
            Document doc = obtenirDocument(entradaXML);
            Element arrel = doc.getDocumentElement();
            List<MalaltiaPacient> malalties = new ArrayList<>();

            // Comencem el recorregut recursiu per l'arrel, al nivell 0
            recorrer(arrel, 0,new ArrayList<>(),malalties);
            return malalties;
    }

    /**
     * Carrega el fitxer XML (Path de NIO) i retorna l'arbre DOM.
     * parse() no accepta un Path, per això li passem un InputStream de NIO.
     */
    public static Document obtenirDocument(Path entradaXML)
            throws IOException, ParserConfigurationException, SAXException {

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        try (InputStream is = Files.newInputStream(entradaXML)) {
            Document doc = builder.parse(is);
            doc.getDocumentElement().normalize();   // fusiona nodes de text adjacents
            return doc;
        }
    }

    /**
     * Recorre recursivament l'arbre a partir d'un node.
     *  - Cas base 1: el node no és un element (text de la indentació) -> no fem res.
     *  - Cas base 2: element fulla (només conté text) -> el mostrem i tornem.
     *  - Cas recursiu: element pare -> el mostrem i explorem els seus fills.
     *
     * node:   node que estem visitant
     * nivell: profunditat dins l'arbre (0 = arrel, 1 = registres com <alumne>..., 
     *                                   2 = registres com <nom>)
     */
    public static void recorrer(Node node, int nivell, List<Pacient> pacients, List<MalaltiaPacient> malPacients) {
        
        // Cas base 1: només tractem elements; els nodes de text de la indentació s'ignoren
        if (node.getNodeType() != Node.ELEMENT_NODE) return;

        // Afegim els atributs de l'element (p. ex. id), sigui quin sigui el seu nom
        NamedNodeMap atributs = node.getAttributes();
        for (int i = 0; i < atributs.getLength(); i++) {
            Node at = atributs.item(i);
            if (node.getNodeName().equals("telecom")) {
                if (at.getNodeName().equals("value")) {
                    if (at.getNodeValue().startsWith("tel:")) {
                        p.setTelephone(at.getNodeValue().substring(4));
                    }
                    if (at.getNodeValue().startsWith("mailto:")) {
                        p.setEmail(at.getNodeValue().substring(7));
                    }
                }
            }
            if (node.getNodeName().equals("administrativeGenderCode")) {
                if (at.getNodeName().equals("code")) {
                    p.setGender(at.getNodeValue());
                }
            }
            if (node.getNodeName().equals("birthTime")) {
                if (at.getNodeName().equals("value")) {
                    String valor = at.getNodeValue();
                    p.setBirthDate(LocalDate.of(
                            Integer.parseInt(valor.substring(0, 4)),
                            Integer.parseInt(valor.substring(4, 6)),
                            Integer.parseInt(valor.substring(6, 8))));
                }
            }
            if (node.getNodeName().equals("code") && mp != null) {
                if (at.getNodeName().equals("code")) {
                    mp.setCode(at.getNodeValue());
                }
            }
            if (node.getNodeName().equals("dose")) {
                if (at.getNodeName().equals("value")) {
                    dosi = at.getNodeValue();
                }
                if (at.getNodeName().equals("unit")) {
                    unitat = at.getNodeValue();
                }
            }
        }
 
        // Cas base 2: element fulla (nom, cognoms...) -> mostrem nom i text a la mateixa línia
        if (esFulla(node)) {
            if (node.getNodeName().equals("given")) {
                p.setGivenName(node.getTextContent());
            }
            if (node.getNodeName().equals("family")) {
                p.setFamilyName(node.getTextContent());
            }
            if (node.getNodeName().equals("dni")) {
                p.setDni(node.getTextContent());
            }
            if (node.getNodeName().equals("streetAddressLine")) {
                p.setAddress(node.getTextContent());
            }
            if (node.getNodeName().equals("city")) {
                p.setCity(node.getTextContent());
            }
            if (node.getNodeName().equals("postalCode")) {
                p.setPostalCode(node.getTextContent());
            }
            if (node.getNodeName().equals("country")) {
                p.setCountry(node.getTextContent());
            }
            if (node.getNodeName().equals("code") && mp != null) {
                mp.setName(node.getTextContent());
            }
            if (node.getNodeName().equals("status") && mp != null) {
                mp.setStatus(node.getTextContent());
            }
            if (node.getNodeName().equals("medication") && mp != null) {
                medicacio = node.getTextContent();
            }
            if (node.getNodeName().equals("frequency") && mp != null) {
                frequencia = node.getTextContent();
            }
            return;
        }

        // Element pare significa que creem un nou pacient
        if (nivell == 1) {
            p = new Pacient();
        } 

        if (node.getNodeName().equals("problem")) {
            mp = new MalaltiaPacient();
            tractament = "";
            if (p != null) {
                mp.setDniPacient(p.getDni());
            }
        }

        if (node.getNodeName().equals("treatment")) {
            medicacio = null;
            dosi = null;
            unitat = null;
            frequencia = null;
        }

        // Cas recursiu: explorem els fills un nivell més avall
        NodeList fills = node.getChildNodes();
        for (int i = 0; i < fills.getLength(); i++) {
            recorrer(fills.item(i), nivell + 1,pacients,malPacients);
        }

        if (node.getNodeName().equals("treatment") && mp != null) {
            String text = "";
            if (medicacio != null) {
                text = medicacio;
            }
            if (dosi != null) {
                text = text + " " + dosi;
            }
            if (unitat != null) {
                text = text + " " + unitat;
            }
            if (frequencia != null) {
                text = text + ", " + frequencia;
            }
            if (tractament.length() > 0) {
                tractament = tractament + "; ";
            }
            tractament = tractament + text;
            mp.setTreatment(tractament);
        }
        if (node.getNodeName().equals("problem")) {
            malPacients.add(mp);
            mp = null;
        }
        if (nivell == 1) {
            pacients.add(p);
            p = null;
        }
    }

    /**
     * Un element és fulla si no té cap fill de tipus element (només text, o res).
     * Atenció: un element buit també es considera fulla.
     */
    static boolean esFulla(Node node) {
        NodeList fills = node.getChildNodes();
        for (int i = 0; i < fills.getLength(); i++) {
            if (fills.item(i).getNodeType() == Node.ELEMENT_NODE) return false;
        }
        return true;
    }
}
