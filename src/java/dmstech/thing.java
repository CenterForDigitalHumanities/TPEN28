/*
 * @author Jon Deering
Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License.

You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
and limitations under the License.
 */
package dmstech;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import static com.hp.hpl.jena.query.QueryExecutionFactory.create;
import com.hp.hpl.jena.query.QueryFactory;
import static com.hp.hpl.jena.query.QueryFactory.create;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import static com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel;
import static dmstech.manifestAggregation.getManifestUrls;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.System.out;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.Stack;

/**
 *
 * @author ijrikgnmd
 */
public class thing {

    /**
     * (TODO: Complete.)
     *
     * @param m3UrlString A URI to the XML file/data that describes the
     * manifests.
     *
     * @return String[]
     * @throws IOException
     */
    public static String[] getManifestAggregations(String m3UrlString) throws IOException //public static String[] getCollectionManifestURIs(String m3UrlString) throws IOException
    {
        String[] toret = null;
        Stack<String> urls = new Stack();
        String format = "";
        URL m3Url = new URL(m3UrlString);
        HttpURLConnection m3connection = null;

        // set up our connection for downloading
        m3connection = (HttpURLConnection) m3Url.openConnection();
        m3connection.setRequestMethod("GET");
        m3connection.setRequestProperty("accept", "application/xml");
        m3connection.setDoOutput(true);
        m3connection.setReadTimeout(10000);
        m3connection.connect();

        // determine our format
        if (m3UrlString.toLowerCase().endsWith("n3")) {
            format = "N3";
        } else {
            format = "";
        }

        // open our input stream
        BufferedReader m3Reader = null;
        m3Reader = new BufferedReader(new InputStreamReader(m3connection.getInputStream()));

        // create our model (BOZO:  What does this actually do?)
        Model m3Model = createDefaultModel();
        m3Model.read(m3Reader, null, format);

        // get all aggregations as a {subject}-{obj} pair,
        // where each {obj} is the base URI for a collection
        // (e.g., "http://dms-data.stanford.edu/Walters/Collection")
        String queryString = "prefix ore:<http://www.openarchives.org/ore/terms/> select * where{   ?subject ore:aggregates ?obj  }";
        Query query = QueryFactory.create(queryString);
        QueryExecution qe = create(query, m3Model);
        ResultSet results = qe.execSelect();

        // for each aggregation, find the URI for the normal sequence
        // (e.g., "http://dms-data.stanford.edu/Walters/Collection.xml")
        while (results.hasNext()) {
            QuerySolution qs = results.next();
//            System.out.print("found SUBJECT="+qs.get("subject").toString()+"\n");
//            System.out.print("found OBJ="+qs.get("obj").toString()+"\n");

            // get all subjects that describe our target {obj}
            // (so, get all manifest URIs)
            queryString = "prefix ore:<http://www.openarchives.org/ore/terms/> select * where{ ?sub ore:describes <" + qs.get("obj") + "> }";
            query = QueryFactory.create(queryString);
            qe = create(query, m3Model);
            ResultSet results2 = qe.execSelect();
            while (results2.hasNext()) {
                qs = results2.next();
                urls.push(qs.get("sub").toString());
            }
        }

        // convert our Stack<> to String[] and return our array of manifest URLs
        toret = new String[urls.size()];
        for (int i = 0; i < toret.length; i++) {
            toret[i] = urls.pop();
        }
        return toret;
    }

    /**
     * TODO: Complete.
     *
     * @param args
     * @throws IOException
     * @throws SQLException
     */
    public static void main(String[] args) throws IOException, SQLException {
        String[] res = getManifestAggregations("http://dms-data.stanford.edu/Repository.xml");
        //String [] res=new String[]{"http://rosetest.library.jhu.edu/m3"};
        for (String re : res) {
            String[] p = getManifestUrls(re);
            for (String p1 : p) {
                out.print(p1 + "\n");
                try {
                    sequence s = new sequence(new URL[]{new URL(p1)}, "");
                    out.append(s.getSequenceItems().length + "\n");
                    // get all items in the sequence; print URLs for all of them
                    canvas[] c = s.getSequenceItems();
                    for (canvas c1 : c) {
                        out.print(c1.getImageURL()[0].getImageURL() + "\n");
                    }
                } catch (IOException | SQLException e) {
                }
            }
        }
    }
}
