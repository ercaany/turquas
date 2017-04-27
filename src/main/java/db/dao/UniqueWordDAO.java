package db.dao;

import com.datastax.driver.core.*;
import db.CassandraConfiguration;
import model.UniqueWord;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mustafa on 26.04.2017.
 */
public class UniqueWordDAO {
    private Session session;
    private PreparedStatement preparedStatement;


    public Set<UniqueWord> getAllWords() {
        String query = "SELECT * FROM unique_word";
        session = CassandraConfiguration.getCLuster().connect("turquas");
        ResultSet result = session.execute(query);
        Set<UniqueWord> uniqueWordSet = new HashSet<UniqueWord>();

        for(Row row: result) {
            UniqueWord uniqueWord = new UniqueWord(row.getString(0));
            uniqueWord.setSourceSet(row.getSet(1, String.class));
            uniqueWordSet.add(uniqueWord);
        }

        session.close();
        return uniqueWordSet;
    }

    public boolean update(Set<UniqueWord> uniqueWordSet) {
        try{
            BatchStatement batch = new BatchStatement();
            prepareForUpdate();
            session = CassandraConfiguration.getCLuster().connect("turquas");


            for(UniqueWord uniqueWord: uniqueWordSet){
                BoundStatement bound = preparedStatement.bind(uniqueWord.getValueMap(), uniqueWord.getWord());
                batch.add(bound);
            }

            session.execute(batch);
            session.close();
        } catch(Exception ex){
            System.out.println(ex.getMessage());
            return false;
        }

        return true;
    }

    public void prepareForUpdate(){
        preparedStatement = session.prepare(
                "UPDATE unique_word SET value = ? WHERE word = ?");
    }
}
