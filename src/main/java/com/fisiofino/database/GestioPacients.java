package com.fisiofino.database;


import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import com.fisiofino.model.MalaltiaPacient;
import com.fisiofino.model.Pacient;

public class GestioPacients {

//Apartat 1: Insercions a partir del XML
    public int CreatePatient(Pacient p) throws SQLException {

        int idPacient = 0;

        Pacient pacientExistente = findBydniNie(p.getDni());
        if (pacientExistente != null) {
            return pacientExistente.getIdPatient();
        }

        String sql = "INSERT INTO patient "
                + "(given_name, family_name, nif, birth_date, gender, address, city, postal_code, country, telephone, email) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Connexio.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getGivenName());
            ps.setString(2, p.getFamilyName());
            ps.setString(3, p.getDni());
            ps.setDate(4, p.getBirthDate() == null ? null : Date.valueOf(p.getBirthDate()));
            ps.setString(5, p.getGender());
            ps.setString(6, p.getAddress());
            ps.setString(7, p.getCity());
            ps.setString(8, p.getPostalCode());
            ps.setString(9, p.getCountry());
            ps.setString(10, p.getTelephone());
            ps.setString(11, p.getEmail());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    idPacient = keys.getInt(1);
                }
            }
        }

        return idPacient;
    }

    public boolean CreateMalaltia(MalaltiaPacient mp) throws SQLException {

        boolean malaltiaCreada = false;

        String sql = "INSERT INTO disease (snomed_code, name) VALUES (?, ?)";

        try (Connection conn = Connexio.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mp.getCode());
            ps.setString(2, mp.getName());
            malaltiaCreada = ps.executeUpdate() > 0;
        }
        
        return malaltiaCreada;
    }    

    public boolean CreateMalaltiaPacient(MalaltiaPacient mp) throws SQLException {
        /* Son casi tdas igual hacer try con la conexion y prepararlo cogiendo lo q quiero usar */
        boolean malaltiaPacientCreada = false;

        Pacient pacient = findBydniNie(mp.getDniPacient());
        if (pacient == null) {
            return malaltiaPacientCreada;
        }

        String sql = "INSERT INTO patient_disease "
                + "(id_patient, snomed_code, status, treatment) VALUES (?, ?, ?, ?)";

        try (Connection conn = Connexio.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pacient.getIdPatient());
            ps.setString(2, mp.getCode());
            ps.setString(3, mp.getStatus());
            ps.setString(4, mp.getTreatment());
            malaltiaPacientCreada = ps.executeUpdate() > 0;
        }
        
        return malaltiaPacientCreada;
    }    

//Apartat 2: Cerca de pacient per DNI
    public Pacient findBydniNie(String dni_nie) throws SQLException {
            
        Pacient p = null;

        String sql = "SELECT id_patient, given_name, family_name, nif, birth_date, gender, address, "
                + "city, postal_code, country, telephone, email "
                + "FROM patient WHERE nif = ?";

        try (Connection conn = Connexio.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dni_nie);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p = new Pacient();
                    p.setIdPatient(rs.getInt("id_patient"));
                    p.setGivenName(rs.getString("given_name"));
                    p.setFamilyName(rs.getString("family_name"));
                    p.setDni(rs.getString("nif"));
                    Date birthDate = rs.getDate("birth_date");
                    p.setBirthDate(birthDate == null ? null : birthDate.toLocalDate());
                    p.setGender(rs.getString("gender"));
                    p.setAddress(rs.getString("address"));
                    p.setCity(rs.getString("city"));
                    p.setPostalCode(rs.getString("postal_code"));
                    p.setCountry(rs.getString("country"));
                    p.setTelephone(rs.getString("telephone"));
                    p.setEmail(rs.getString("email"));
                }
            }
        }

        return  p;
    }

//Apartat 3: Malalties del pacient
    public List<MalaltiaPacient> findDiseaseBydniNie(String dni_nie) throws SQLException {
        List<MalaltiaPacient> malaltiespacient = new ArrayList<MalaltiaPacient>();

        String sql = "SELECT disease.snomed_code, disease.name, patient_disease.status, "
                + "patient_disease.treatment, patient.nif "
                + "FROM patient "
                + "JOIN patient_disease ON patient.id_patient = patient_disease.id_patient "
                + "JOIN disease ON patient_disease.snomed_code = disease.snomed_code "
                + "WHERE patient.nif = ?";

        try (Connection conn = Connexio.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dni_nie);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MalaltiaPacient mp = new MalaltiaPacient();
                    mp.setCode(rs.getString("snomed_code"));
                    mp.setName(rs.getString("name"));
                    mp.setStatus(rs.getString("status"));
                    mp.setTreatment(rs.getString("treatment"));
                    mp.setDniPacient(rs.getString("nif"));
                    malaltiespacient.add(mp);
                }
            }
        }

        return malaltiespacient;
    }

//Apartat 4: Llistar tots els pacients
    public List<Pacient> findAllPacients () throws SQLException {
        List<Pacient> pacients = new ArrayList<>();

        String sql = "SELECT * FROM patient";

        try (Connection conn = Connexio.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Pacient p = new Pacient();
                p.setIdPatient(rs.getInt("id_patient"));
                p.setGivenName(rs.getString("given_name"));
                p.setFamilyName(rs.getString("family_name"));
                p.setDni(rs.getString("nif"));
                Date birthDate = rs.getDate("birth_date");
                p.setBirthDate(birthDate == null ? null : birthDate.toLocalDate());
                p.setGender(rs.getString("gender"));
                p.setAddress(rs.getString("address"));
                p.setCity(rs.getString("city"));
                p.setPostalCode(rs.getString("postal_code"));
                p.setCountry(rs.getString("country"));
                p.setTelephone(rs.getString("telephone"));
                p.setEmail(rs.getString("email"));

                pacients.add(p);
            }
        }
        /*Con el while recorro todo lo dl paciente y lo añado a mi lista de pacientes */
        return pacients;
    }

//Apartat 5: Modificar l'estat d'una malaltia d'un pacient
    public boolean updateStatus (int idPacient, String snomedCode, String status) throws SQLException {
        boolean modificat = false;

        String sql = "UPDATE patient_disease SET status = ? "
        + "WHERE id_patient = ? AND snomed_code = ?";

        try (Connection conn = Connexio.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, idPacient);
            ps.setString(3, snomedCode);

            modificat = ps.executeUpdate() > 0;
        }

        return  modificat;
    }

// funcions i mètodes accesoris i necessaris per saber si s'ha d'insertar o modificar    
    public boolean findMalaltia (String SnomedCode) throws SQLException {
        boolean esMalaltia = false;
        
        String sql = "SELECT * FROM disease WHERE snomed_code = ?";
        try(Connection conn = Connexio.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, SnomedCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    esMalaltia = true;
                }
            }
        }
        
        return esMalaltia;
    }

    public boolean findMalaltiaPacient (String dniPacient, String SnomedCode, String status) throws SQLException {
        boolean esMalaltiaPacient = false;

        String sql = "SELECT * FROM patient_disease "
                + "JOIN patient ON patient.id_patient = patient_disease.id_patient "
                + "WHERE patient.nif = ? AND patient_disease.snomed_code = ? AND patient_disease.status = ?";
        
        try(Connection conn = Connexio.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dniPacient);
            ps.setString(2, SnomedCode);
            ps.setString(3, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    esMalaltiaPacient = true;
                }
            }
        }

        return esMalaltiaPacient;
    }

}
