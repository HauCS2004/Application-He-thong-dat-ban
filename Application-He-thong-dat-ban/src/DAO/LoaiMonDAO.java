package DAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import connectDB.ConnectDB;
import Entity.LoaiMon;

public class LoaiMonDAO {
    public ArrayList<LoaiMon> getAllLoai() {
        ArrayList<LoaiMon> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT * FROM LoaiMon";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while(rs.next()) {
                list.add(new LoaiMon(rs.getString(1), rs.getString(2)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}