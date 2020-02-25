/*
 * Copyright 2011-2013 Saint Louis University. Licensed under the
 *	Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * @author Jon Deering
 */package detectimages;

import static detectimages.blob.getBlobs;
import static detectimages.blob.writeMatchResults;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Math.abs;
import static java.lang.System.out;
import java.util.Vector;
import java.util.concurrent.Callable;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;

/**
 *
 * @author jdeerin1
 */
public class pageComparer implements Callable
{
    Vector <blob>blobs;
    Vector <blob>blobs2;
    String [] assignment;
    String file2;
    blobManager manager;
    private String outputLocation="results/overload/";
    public pageComparer(Vector <blob> blobs,String file2, String [] assignment,blobManager manager)
    {
        this.file2=file2;
        this.blobs=blobs; //rememeber the name
        this.manager=manager;
        this.assignment=assignment;
       
    }
    public void setOutputLocation(String n)
    {
        this.outputLocation=n;
    }

    public Object call() 
    {

        if(file2!=null)
             try {
            blobs2 = manager.get(file2);
            if(blobs2==null)
            {
                manager.add(getBlobs(file2), file2);
                blobs2=manager.get(file2);
                    
            }



         } catch (Exception ex) {
        }
        FileWriter w = null;
        blob [] b1=new blob[blobs.size()];
       
        for(int i=0;i<b1.length;i++)
        {
            b1[i]=blobs.get(i);
           
        }
        blob [] b2=new blob[blobs2.size()];
        
        for(int i=0;i<b2.length;i++)
        {
            b2[i]=blobs2.get(i);
            
            
        }
        try {
            w = (new FileWriter(this.outputLocation + assignment[0] + " " + ".txt",true));
            int matches = 0;
            int matches2 = 0;
            StringBuilder res=new StringBuilder("");
            for (blob b11 : b1) {
                for (blob b21 : b2) {
                    int biggest=0;
                    if (b11.size > b21.size) {
                        biggest = b11.size;
                    } else {
                        biggest = b21.size;
                    }
                    if (b11.size > 25 && b21.size > 25 && (abs(b11.size - b21.size) < biggest*.3)) {
                        //blobComparer b = new blobComparer(b1[i], b2[j]);
                        //double tmp =  b.run();
                        //altBlob a=b1[i].altVersion;
                        //altBlob b=b2[j].altVersion;

                        //double tmp=b1[i].altVersion.run(b2[j].altVersion);
                        double tmp;
                        double tmp2;
                        if (true || (b11.matrixVersion.matrix.length - b21.matrixVersion.matrix.length > 3 || b21.matrixVersion.matrix.length - b11.matrixVersion.matrix.length > 3)) {
                            if (b11.matrixVersion.matrix.length < b21.matrixVersion.matrix.length) {
                                //tmp=b1[i].matrixVersion.compareWithScaling(b2[j].matrixVersion);
                                //tmp=b1[i].matrixVersion.compareToWithAdjust(b2[j].matrixVersion.scaleMatrixBlob(b2[j],b1[i].matrixVersion.matrix.length));
                                tmp = b11.matrixVersion.compareToWithAdjust(b21.matrixVersion);
                            } else {
                                //tmp=b1[i].matrixVersion.compareWithScaling(b2[j].matrixVersion);
                                //tmp=b1[i].matrixVersion.scaleMatrixBlob(b1[i],b2[j].matrixVersion.matrix.length).compareToWithAdjust(b2[j].matrixVersion);
                                tmp = b11.matrixVersion.compareToWithAdjust(b21.matrixVersion);
                            }
                            {}
                        }
                        //tmp=0.0;
                        //tmp=0.0;
                        //tmp=0.0;
                        //tmp=b1[i].matrixVersion.compareWithScaling(b2[j].matrixVersion);
                        //tmp=b1[i].matrixVersion.compareWithScaling(b2[j].matrixVersion);
                        //tmp=b1[i].matrixVersion.compareWithScaling(b2[j].matrixVersion);
                        //tmp=b1[i].matrixVersion.compareToWithAdjust(b2[j].matrixVersion);  //
                        //tmp=b1[i].matrixVersion.compareToWithAdjust(b2[j].matrixVersion);  //
                        //tmp=b1[i].matrixVersion.compareToWithAdjust(b2[j].matrixVersion);  //
                        //tmp2=b1[i].altVersion.run(b2[j].altVersion);
                        //tmp2=b1[i].altVersion.run(b2[j].altVersion);
                        //tmp2=b1[i].altVersion.run(b2[j].altVersion);
                        // tmp2=(double) tmp2 /biggest;
                        // tmp2=(double) tmp2 /biggest;
                        // tmp2=(double) tmp2 /biggest;
                        //else
                        {
                        //tmp=0.0;
                        //tmp=b1[i].matrixVersion.compareWithScaling(b2[j].matrixVersion);
                        //tmp=b1[i].matrixVersion.compareToWithAdjust(b2[j].matrixVersion);  //
                        //tmp2=b1[i].altVersion.run(b2[j].altVersion);
                        
                        
                        // tmp2=(double) tmp2 /biggest;
                        
                    }
                        //             tmp=(double) tmp / biggest;
                        //tmp=tmp2;
                        //tmp=tmp2;
                        if (tmp > 0.7) {
                            matches++;
                            try{
//b1[i].matrixVersion.writeGlyph(assignment[0]+assignment[1]+i+j+"a", b2[j].matrixVersion);
//System.out.print("tmp:"+tmp+"\n");
                            }
                            catch(Exception e)
                            {
                            }
                            //                     imageHelpers.writeImage(toret, "/usr/glyphs/"+assignment[0]+assignment[1]+i+j+".jpg");
                            //blob.writeMatchResults(b1[i].id, b2[j].id, assignment, w);
                            w.flush();
                            //String a="\"insert into blobs(img1, blob1,img2,blob2) values ('"+assignment[0] + "','" + b1[i].id + "','" + assignment[1] + "','" + b2[j].id + "');\n";
                            //String a="\""+assignment[0] + "\",\"" + b1[i].id + "\",\"" + assignment[1] + "\",\"" + b2[j].id + "\"\n";
                            int tmpInt=(int) (tmp*100);
                            String a = assignment[0] + ":" + b11.id + ";" + assignment[1] + ":" + b21.id + "/" + tmpInt + "\n";
                            res.append(a);
                            //blob.writeMatchResults(i, j, assignment, w);
                        } else {
                            //System.out.print("tmp:"+tmp+"\n");
                            //System.out.print("tmp:"+tmp+"\n");
                            //System.out.print("tmp:"+tmp+"\n");
                        }
                        /*if(tmp>0.7 && tmp2<0.7)
                        System.out.print("old val "+tmp+" new val "+tmp2+"\n");
                        if(tmp<=0.7 && tmp2>0.7)
                        System.out.print("foundnew old val "+tmp+" new val "+tmp2+"\n");*/
                    }
                }
                //if the overlap of the 2 images is more than 70%, it is a good match
                //if the overlap of the 2 images is more than 70%, it is a good match
            }
            writeMatchResults(res.toString(),w);
            w.flush();
            w.close();
            out.print(assignment[0] + ":" + assignment[1] + ":" + matches + "\n");
            return assignment[0] + ":" + assignment[1] + ":" + matches + "\n";
        } catch (IOException ex) {
            out.print("caught error\n");
                              getLogger(pageComparer.class.getName()).log(SEVERE, null, ex);
        } finally {
            try {

                w.close();
                return "bad1";
            } catch (IOException ex) {

                getLogger(pageComparer.class.getName()).log(SEVERE, null, ex);
                return "bad2";
            }
        }
    }

}
