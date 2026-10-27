package ensdfparser.check;

import java.util.Vector;

import ensdfparser.ensdf.DatasetID;
import ensdfparser.ensdf.ENSDF;
import ensdfparser.ensdf.JPI;
import ensdfparser.ensdf.Level;
import ensdfparser.ensdf.Nucleus;
import ensdfparser.nds.ensdf.EnsdfUtil;
import ensdfparser.nds.util.Str;

public class TransferInfo {

	DatasetID id=null;
	boolean isTransfer=false;
	boolean isUniqueTargetJPI=false;
	
	boolean isInverse=false;
	boolean isKnockout=false;//treated as a special transfer
	
	String targetParity="",targetSpin="",transferJS="",targetJPS="";
	int targetParityCode=0;
	
	Vector<JPI> targetJPIsV=new Vector<JPI>();
	
	public TransferInfo(ENSDF ens){
		
		try {
			String DSID=ens.DSId0();
			id=new DatasetID(DSID);
			String beam=id.beam;
			if(beam.startsWith("POL "))
				beam=beam.substring(4).trim();
			
            //System.out.println(DSID+"  beam="+id.beam+" ejectile="+id.ejectile);
            
            if(DSID.contains("DECAY") || DSID.contains("RESONANCE")) {
            	isTransfer=false;
            	return;
            }
			
			if(beam.isEmpty() || id.ejectile.isEmpty() || id.ejectile.startsWith("G")) {
				isTransfer=false;
				return;
			}

			String ejectile=id.ejectile;
			
			Nucleus target=ens.target();
			
			int AT=ens.target().A();
			int ZT=ens.target().Z();
			
			int A=ens.nucleus().A();
			int Z=ens.nucleus().Z();

			
			int DA=Math.abs(AT-A);	

			//System.out.println("TransferInfo 61: DSID="+ens.DSId()+" DA="+DA);
			
			if(DA>4) {

				//check if the reaction is inverse kinematics
				Nucleus beamNuc=new Nucleus(beam);
	            int AB=beamNuc.A();
	            int ZB=beamNuc.Z();
	            int tempDA=Math.abs(AB-A);
	            
				if(AT>AB) {
					//e.g., 160GD(37CL,XG)35P
					isTransfer=false;
					return;
				}
				
	            //System.out.println("TransferInfo 61: DSID="+ens.DSId()+" DA="+DA+" tempDA="+tempDA+" "+ens.nLevWL());
	            
	            if(tempDA<4 && beamNuc.A()>AT && ens.nLevWL()>0) {
	            	isInverse=true;
	            	
	            	AT=AB;
	            	ZT=ZB;
	            	
	            	target=beamNuc;
	            	
	            	beam=id.target;
	            	if(beam.equals("1H"))
	            		beam="P";
	            	else if(beam.equals("2H"))
	            		beam="D";
	            	else if(beam.equals("3H"))
	            		beam="T";
	            	else if(beam.equals("4HE"))
	            		beam="A";

	            	AB=ens.target().A();
	            	ZB=ens.target().Z();
	            	
	            	DA=Math.abs(AT-A);
	            			            
	            	if(AB<=0 || ZB<0 || (AT-A)*(ZT-Z)<0) {
	            		isTransfer=false;
	            		return;
	            	}
	            	if(ejectile.startsWith(ens.nucleus().nameENSDF().trim())) {
	            		
	            		
	            		int A1=AB+AT-A;
	            		int Z1=ZB+ZT-Z;
	            		Nucleus ejecNuc=new Nucleus(Z1,A1);
	            		if(ejecNuc.En().isEmpty()) {
	            			isTransfer=false;
	            			return;
	            		}
	            		ejectile=ejecNuc.nameENSDF().trim().toUpperCase();
		            	if(ejectile.equals("1H"))
		            		ejectile="P";
		            	else if(ejectile.equals("2H"))
		            		ejectile="D";
		            	else if(ejectile.equals("3H"))
		            		ejectile="T";
		            	else if(ejectile.equals("4HE"))
		            		ejectile="A";
		            	else if(ejectile.equals("1N") || ejectile.equals("1NN")) {
		            		ejectile="N";
		            	}else if(A1>4 && (AT-A)==1 ) {
		            		//one-nucleon knock-out reaction with L-transfer measured
		            		//e.g., 9BE(36S,35PG), with L-transfer and gamma data
		            		isKnockout=true;
		            		if((ZT-Z)==1) {
		            			//one-proton knock-out, here treated the same way as (n,d)
		            			beam="N";
		            			ejectile="D";
		            		}else if((ZT-Z)==0) {
		            			//one-neutron knock-out, here treated the same way as (p,d)
		            			beam="P";
		            			ejectile="D";
		            		}else {
		            			isTransfer=false;
		            			return;
		            		}
		            	}
	            	}
	            	
	            	//System.out.println("TransferInfo 102: DSID="+ens.DSId()+" DA="+DA+" beam="+beam+" AB="+AB+" AT="+AT+" ej="+ejectile);

	            		
	            }
			}
            
			boolean isEvenEven=target.isEvenEven();
			
			
			String reaction="("+beam+","+ejectile+")";
			
			targetParityCode=0;
			
			//spin of transfered particles
			//final spin J=||L-JT|-jp| to L+JT+jp, where JT is the target spin
			//final PI=PI(target)*(-1)^L*PI(transfered particles)=PI(target)*(-1)^L
			//  (from statistics of all transfer reactions in ENSDF, PI(transfered particles)=+)
			
			//System.out.println(" AT="+AT+" A="+A);
			
			transferJS="";
			targetSpin="";
			targetJPS="";
			if(DA==1) {
		        Nucleus nuc1=new Nucleus(beam);
		        Nucleus nuc2=new Nucleus(ejectile);
		        int dA=Math.abs(nuc1.A()-nuc2.A());
		        int dZ=Math.abs(nuc1.Z()-nuc2.Z());
			    if(nuc1.A()<=0 || nuc2.A()<=0 || nuc1.Z()<0 || nuc2.Z()<0 || dA!=1 || dZ>1) {
			        isTransfer=false;
			        return;
			    }
			    
				transferJS="1/2+";
			}else if(DA==2) {
				//j-transfer=0 for (P,T), (T,P), (3HE,N), (N,3HE)
				//j-transfer=1 for (D,A), (A,D)
				//j-transfer=0 or 1 for (3HE,P),(P,3HE)
				if("(P,T)(T,P)(3HE,N)(N,3HE)(A,2HE)(2HE,A)".contains(reaction)) {
					transferJS="0+";
				}else if("(D,A)(A,D)".contains(reaction)) {
					transferJS="1+";
				}else if("(3HE,P)(P,3HE)".contains(reaction)) {
					transferJS="0+,1+";
				}
			}else if(DA==3) {
				//j-transfer=1/2 for (P,A), (A,P), others are much less, like (6LI,T), (3HE,6HE)
				if("(P,A)(A,P)".contains(reaction)) {
					transferJS="1/2+";
				}
			}else if(DA==0) {
				if("(D,D)(D,D')(A,A)(A,A')".contains(reaction))
					transferJS="0+";
				else if("(3HE,T)(T,3HE)(P,P)(P,P')".contains(reaction))
					transferJS="0+,1+";
			}else if(DA==4) {
				if("(D,6LI)(6LI,D)".contains(reaction)) {
					transferJS="0+";
				}
			}
			
			if(isEvenEven) {//target even-even
				
				//target even-even, 
				//DA=1: j-transfer=1/2, final J=L+/-1/2, PI=(-1)^L
				//DA=2: j-transfer=0 or 1, final J=range  PI=(-1)^L,  e.g., (p,t), (t,p), (p,3He), (3He,n)
				//DA=3: j-transfer=1/2 or others, final J=range, PI=(-1)^L,  e.g., (p,a), (a,p),(3He,6He)
				//DA=others: do nothing

			    setTargetJPS("0+");
								
			}else {
				//target not even-even, DA=any 
				//DA=1:     j-transfer=1/2, final J=JT vector+ L vector+ 1/2, PI=PI(target)*(-1)^L
				//DA=2:     j-transfer=0, final J=|L-JT| to L+JT; or j-transfer=0,1, final J=JT vector+ L vector+ 1. PI=PI(target)*(-1)^L, JT is target JPI
				//DA=other: j-transfer=unknown, pi-transfer-unknown, final J=unknown, PI=unknown
				//          do nothing
				
				//find target spin and parity from comment (targetJPS set at the beginning of checkDataset if transfer reaction)
				String jps=target.JPS().trim();
				if(jps.isEmpty() && !isInverse) 
					jps=EnsdfUtil.parseTargetJPS(ens);
				
				setTargetJPS(jps);				
			}
			
			if(!transferJS.isEmpty())
			    isTransfer=true;
			
			//System.out.println("hello7 "+lev.ES()+" targetJS="+targetJS);
			

		}catch(Exception e) {
		    //e.printStackTrace();
		}

	}
	
	public Vector<String> findLevelJPSFromLtransfer(Level lev) {
		return findLevelJPSFromLtransfer(lev,6);
	}
	/*
	 * if L not given, set upper limit=LupperLimit for L and find possible JPS
	 */
	public Vector<String> findLevelJPSFromLtransfer(Level lev, int LupperLimit) {
		Vector<String> out=new Vector<String>();
		try {
			
			
			//System.out.println("##### "+lev.DSID()+" EL="+lev.ES()+"  LS="+lev.lS());
			
			String LS=lev.lS();
			
			String tempLS=LS.replace("(", "").replace(")","").trim();
			tempLS=tempLS.replace("[", "").replace("]","");
			
			Vector<Integer> LV=new Vector<Integer>();
			Vector<String> LSV=new Vector<String>();
			
			if(tempLS.isEmpty() || LS.contains("[")) {//set maximum L=6 for all transfer reaction by default
				if(LupperLimit>=0) 
					LS="LE "+LupperLimit;
				else
					LS="LE 6";
			}
			
			//System.out.println("TransferInfo 271:DSID="+id.dsid+" ES="+lev.ES()+" LS="+LS+" tempLS="+tempLS);
			
			if(Str.isNumeric(tempLS)) {
				if(Str.isInteger(tempLS)) {
					LV.add(Integer.parseInt(tempLS));
					LSV.add(LS);
				}
			}else {
				if(LS.startsWith("LE")) {		    
					LS=LS.substring(2).trim();
					try {
						int maxL=Integer.parseInt(LS);
						for(int i=0;i<=maxL;i++) {
							LV.add(i);
							LSV.add("("+i+")");
						}
					}catch(Exception e) {}
				}else if(LS.startsWith("LT")) {
					LS=LS.substring(2).trim();
					try {
						int maxL=Integer.parseInt(LS)-1;
						for(int i=0;i<=maxL;i++) {
							LV.add(i);
							LSV.add("("+i+")");
						}
					}catch(Exception e) {}
				}else {
					String[] tempLSA=JPI.parseJPS2Array(tempLS);
					String[] LSA=JPI.parseJPS2Array(LS);
					for(int i=0;i<tempLSA.length;i++) {
						try {
							
							String tempLSelement=tempLSA[i].trim();
							String LSelement=LSA[i].trim();
							
							//System.out.println("###"+s+" "+s.contains("+")+s.split("\\+"));
							
							if(Str.isInteger(tempLSelement)) {
								LV.add(Integer.parseInt(tempLSelement));
								LSV.add(LSelement);
							}else if(tempLSelement.contains("+")) {//eg, L=0+2, or (0+2), or 0+(2)
								String[] tempLSA1=tempLSelement.split("\\+");
								String[] tempLSA2=LSelement.split("\\+");
								
								if(tempLSA1.length==2 && tempLSA2.length==2) {
									int L1=Integer.parseInt(tempLSA1[0].trim());
									int L2=Integer.parseInt(tempLSA1[1].trim());
									
									String LS1=tempLSA2[0];
									String LS2=tempLSA2[1];
									boolean isTentativeL1=(LS1.contains("(")||LS1.contains(")"));
									boolean isTentativeL2=(LS2.contains("(")||LS2.contains(")"));
									
									Vector<String> jsV1=findLevelJPSFromLtransfer(L1,isTentativeL1);
									Vector<String> jsV2=findLevelJPSFromLtransfer(L2,isTentativeL2);
									
									//System.out.println(" id="+id.dsid+"  targetJ="+targetSpin+" transferJ="+transferJS+" L1="+L1+" L2="+L2);
									//for(String s1:jV2) System.out.println(" J in jV2: "+s1);
									
									jsV1=JPI.mergeJSV(jsV1,jsV2,false);//false for "AND" operation
									if(jsV1!=null) {
										for(int j=0;j<jsV1.size();j++) {
											out.add(jsV1.get(j));
										}
									}

								}
								
								if(out.isEmpty() && tempLSA1.length>0) {
									boolean hasEvenL=false,hasOddL=false;
									for(int j=0;j<tempLSA1.length;j++) {
										int L=Integer.parseInt(tempLSA1[0].trim());
										if(L%2==1)
											hasOddL=true;
										else 
											hasEvenL=true;
									}
									
									if(hasEvenL&&!hasOddL)
										out.add("+");
									else if(hasOddL&&!hasEvenL)
										out.add("-");
								}
							}else
								return new Vector<String>();
						}catch(Exception e) {
							e.printStackTrace();
							return new Vector<String>();
						}
		
					}
				}

			}
			
			
			for(int i=0;i<LV.size();i++) {
				LS=LSV.get(i);
				
				boolean isTentativeL=(LS.contains("(")||LS.contains(")"));
						
				Vector<String> tempV=findLevelJPSFromLtransfer(LV.get(i),isTentativeL);
				
				//System.out.println("TransferInfo 369: DSID="+id.dsid+" ES="+lev.ES()+" LS="+LS+" "+isTentativeL+" "+tempV.size());
				
				//System.out.println("1@@@@@  lev="+lev.ES()+" "+(out==null)+"  "+out.isEmpty());
				//for(String s:out) System.out.println("$$$$ "+s);
				//for(String s:tempV) System.out.println("#### "+s);
				
				if(tempV.size()>0)
					out=JPI.mergeJSV(out,tempV,true);//true for "OR" operation
				
				//System.out.println("2@@@@@  lev="+lev.ES()+" "+(out==null));
			}

			simplifyJSV(out);
			sortJSV(out);

			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return out;
	}
	
	/*
	public Vector<String> findLevelJPSFromLtransfer_old(Level lev, int LupperLimit) {

		Vector<String> out=new Vector<String>();
		
		//System.out.println("##### "+lev.DSID()+" EL="+lev.ES()+"  LS="+lev.lS());

		
		String LS=lev.lS().replace("(", "").replace(")","").trim();
		LS=LS.replace("[", "").replace("]","");
		
		Vector<Integer> LV=new Vector<Integer>();
		if(LS.isEmpty()) {//set maximum L=6 for all transfer reaction by default
			if(LupperLimit>=0) 
				LS="LE "+LupperLimit;
			else
				LS="LE 6";
		}
		
		if(Str.isNumeric(LS)) {
			if(Str.isInteger(LS))
				LV.add(Integer.parseInt(LS));
		}else {
			if(LS.startsWith("LE")) {
			    
				LS=LS.substring(2).trim();
				try {
					int maxL=Integer.parseInt(LS);
					for(int i=0;i<=maxL;i++)
						LV.add(i);
				}catch(Exception e) {}
			}else if(LS.startsWith("LT")) {
				LS=LS.substring(2).trim();
				try {
					int maxL=Integer.parseInt(LS)-1;
					for(int i=0;i<=maxL;i++)
						LV.add(i);
				}catch(Exception e) {}
			}else {
				String[] la=LS.split(",");
				for(int i=0;i<la.length;i++) {
					try {
						
						String s=la[i].trim();
						
						//System.out.println("###"+s+" "+s.contains("+")+s.split("\\+"));
						
						if(Str.isInteger(s))
							LV.add(Integer.parseInt(s));
						else if(s.contains("+")) {//eg, L=0+2
							String[] laa=s.split("\\+");
		
							if(laa.length==2) {
								int L1=Integer.parseInt(laa[0].trim());
								int L2=Integer.parseInt(laa[1].trim());
								Vector<String> jV1=findLevelJPSFromLtransfer(L1);
								Vector<String> jV2=findLevelJPSFromLtransfer(L2);
								
								//System.out.println(" id="+id.dsid+"  targetJ="+targetSpin+" transferJ="+transferJS+" L1="+L1+" L2="+L2);
								//for(String s1:jV2) System.out.println(" J in jV2: "+s1);
								
								for(int j=0;j<jV1.size();j++) {
									String js=jV1.get(j);
									if(jV2.contains(js) && !out.contains(js))
										out.add(js);
									
									//System.out.println("    js="+js+" jV2.contains(js)="+jV2.contains(js)+" out.contains(js)="+out.contains(js));
								}
							}
							
							if(out.isEmpty() && laa.length>0) {
								boolean hasEvenL=false,hasOddL=false;
								for(int j=0;j<laa.length;j++) {
									int L=Integer.parseInt(laa[0].trim());
									if(L%2==1)
										hasOddL=true;
									else 
										hasEvenL=true;
								}
								
								if(hasEvenL&&!hasOddL)
									out.add("+");
								else if(hasOddL&&!hasEvenL)
									out.add("-");
							}
						}else
							return new Vector<String>();
					}catch(Exception e) {
						e.printStackTrace();
						return new Vector<String>();
					}
	
				}
			}

		}

		for(int i=0;i<LV.size();i++) {
			Vector<String> temp=findLevelJPSFromLtransfer(LV.get(i),isTentativeL);
			for(int j=0;j<temp.size();j++) {
				String js=temp.get(j);
				
                //System.out.println("L="+LV.get(i)+" js="+js+" out.size="+out.size());
                
				if(!out.contains(js)) {
				    String s=js.replace("+","").replace("-","");
				    //e.g., if 1/2 exists, js=1/2+ or 1/2- will be skipped
				    //      if js=1/2 (no parity), insert 1/2, and remove 1/2+ and 1/2- if existing
				    if(!out.contains(s)) {
				        if(!s.equals(js))//js has "+" or "-", neither js nor s in out
				            out.add(js);
				        else {//s==js has no "+" or "-"
				            out.add(js);
				            out.remove(s+"+");
				            out.remove(s+"-");
				        }
				    }
					
				}
				
				
			}
		}

		simplifyJSV(out);

		sortJSV(out);

		return out;
	}
	*/
	
	public Vector<String> findLevelJPSFromLtransfer(int L,boolean isTentativeL) {
		Vector<String> allowedJSV=new Vector<String>();
		
		try {
	
		    for(JPI targetJPI:targetJPIsV) {
		        Vector<String> tempV=findLevelJPSFromLtransfer(targetJPI,L,isTentativeL);
		        
		        //System.out.println("TransferInfo 520: DSID="+id.dsid+" targetJPI="+targetJPI.js()+"  L="+L+" "+isTentativeL+" "+tempV.size());
		        
		        for(String s:tempV) {
		            if(!allowedJSV.contains(s))
		                allowedJSV.add(s);
		        }
		    }
		}catch(Exception e) {}
		
		simplifyJSV(allowedJSV);
		sortJSV(allowedJSV);
		
		return allowedJSV;
	}

	
	public void sortJSV(Vector<String> JSV) {
		JPI.sortJSV(JSV);
	}
	
	/*
	 * merge j+ and j- to J 
	 * insert j and remove j+ and j-
	 */
	public void simplifyJSV(Vector<String> JSV) {
		JPI.simplifyJSV(JSV);
	}
	
	
    /*
     * for unique target spin-parity, 
     * transferJS=1/2 or 0, or 0,1
     * 	j-transfer=0 for (P,T), (T,P), (3HE,N), (N,3HE)
	 *	j-transfer=1 for (D,A), (A,D)
	 *  j-transfer=0 or 1 for (3HE,P),(P,3HE)
     */	
    public static Vector<String> findLevelJPSFromLtransfer(JPI targetJPI,String transferJS,int L,boolean isTentativeL) {
        Vector<String> allowedJSV=new Vector<String>();
      
        try {
        	//System.out.println("TransferInfo 564: targetJPI="+targetJPI.js()+" transferJS="+transferJS+"  L="+L+" "+isTentativeL);
        	
            if(L<0 || targetJPI.spin().isEmpty() || transferJS.isEmpty())
                return allowedJSV;
      
            
            Vector<JPI> tempV1=JPI.JPICoupling(targetJPI.js(),transferJS);
            
            
            //System.out.println("TransferInfo 573: L="+L+"  "+tempV1.size()+" "+tempV1.get(0).js());
            
            Vector<JPI> tempV2=new Vector<JPI>();
            String LS=""+L;
            LS+=((L%2==0)?"+":"-");
            
            if(isTentativeL)
            	LS="("+LS+")";
            	
            tempV2.add(new JPI(LS));
            
            //System.out.println("TransferInfo 581: LS="+LS+"  "+tempV1.size()+"  "+tempV2.size());
            
            tempV1=JPI.JPICoupling(tempV1,tempV2);
            
        	//System.out.println("TransferInfo 583: LS="+LS+"  "+tempV1.size()+"  "+tempV2.size());
        	
            for(JPI jp:tempV1)
            	allowedJSV.add(jp.js());
            
        }catch(Exception e) {}
        
        return allowedJSV;
    }
    
    public static Vector<String> findLevelJPSFromLtransfer_old(JPI targetJPI,String transferJS,int L,boolean isTentativeL) {
        Vector<String> allowedJSV=new Vector<String>();
      
        try {
                        
            if(L<0)
                return allowedJSV;
      
            String targetSpin=targetJPI.spin();
            int targetParityCode=targetJPI.parityCode();
            
            int thisParityCode=-1;//+1 for positive parity, -1 for negative, 0 for unknown

            if(!targetSpin.isEmpty() && !transferJS.isEmpty()) {

                int JT=-1,jp=-1;

                //final J=JT (vector+) L (vector+) jp = |max(JT,L,jp)-sum(other two)| to JT+L+jp
                
                if(targetSpin.equals("0") && L==0 && transferJS.equals("0,1")) {//jpS=0,1
                    allowedJSV.add("0");
                    allowedJSV.add("1");
                }else {
                    String tempTransferJS=transferJS;
                    if(transferJS.equals("0,1"))
                        tempTransferJS="1";
                    
                    //System.out.println(" id="+id.dsid+"  targetJ="+targetSpin+" transferJ="+transferJS+" L="+L);
                    
                    try {
                        if(targetSpin.contains("/2"))
                            JT=Integer.parseInt(targetSpin.replace("/2", ""));
                        else
                            JT=Integer.parseInt(targetSpin)*2;
                        
                        if(tempTransferJS.contains("/2"))
                            jp=Integer.parseInt(tempTransferJS.replace("/2", ""));
                        else
                            jp=Integer.parseInt(tempTransferJS)*2;
                        
                        int max=Math.max(JT, 2*L);
                        max=Math.max(max, jp);
                        
                        int maxJF=JT+jp+2*L;
                        int minJF=Math.abs(max-(maxJF-max));
                        if(maxJF%2==0 && minJF%2==0) {
                            minJF=minJF/2;
                            maxJF=maxJF/2;
                            for(int i=minJF;i<=maxJF;i++)
                                allowedJSV.add(i+"");
                        }else if(maxJF%2==1 && minJF%2==1){
                            for(int i=minJF;i<=maxJF;i++) {
                                if(i%2==1)
                                    allowedJSV.add(i+"/2");
                            }
                        }
                    }catch(Exception e) {
                        
                    }
                }
                
                
                //parity
                thisParityCode=targetParityCode*(1-2*(L%2));    
                String parity="";
                if(thisParityCode==1)
                    parity="+";
                else if(thisParityCode==-1)
                    parity="-";
                
                if(!parity.isEmpty()) {
                    for(int i=0;i<allowedJSV.size();i++) {
                        String js=allowedJSV.get(i)+parity;
                        allowedJSV.remove(i);
                        allowedJSV.add(i, js);
                    }                    
                }


            }
        }catch(Exception e) {}
        
        return allowedJSV;
    }
    /*
     * for unique target spin-parity
     */	
    public Vector<String> findLevelJPSFromLtransfer(JPI targetJPI,int L,boolean isTentativeL) {
    	return findLevelJPSFromLtransfer(targetJPI,transferJS,L,isTentativeL);
    }
    	
	/*
	 * set value for an unique spin-parity
	 * If the JPI=s contains multiple JPIs, spin and parity will not be set
	 */
	public void setTargetJPS(String s) {
	    resetTargetJPI();
	    
	    s=s.trim();
	    targetJPS=s;

        if(!s.isEmpty()) {
            String[] jv=EnsdfUtil.parseJPI(s);
            if(jv.length==1) {
                JPI jpi=new JPI(jv[0]);
                targetSpin=jpi.spin();
                targetParity=jpi.parity();
                targetParityCode=jpi.parityCode();
                
                targetJPIsV.add(jpi);
                isUniqueTargetJPI=true;
            }else if(jv.length>1){
                isUniqueTargetJPI=false;
                
                for(int i=0;i<jv.length;i++) {
                    JPI jpi=new JPI(jv[i]);
                    targetJPIsV.add(jpi);
                }
            }            
        }   
	}
	
	public void resetTargetJPI() {
	    targetJPS="";
        targetSpin="";
        targetParity="";
        targetParityCode=0;	    
        
        targetJPIsV.clear();
	}
	public void printTest() {
		System.out.println("TargetJPS       ="+targetJPS);
		System.out.println("TargetSpin      ="+targetSpin);
		System.out.println("TargetParity    ="+targetParity);
		System.out.println("TargetParityCode="+targetParityCode);
		System.out.println("TransferJS      ="+transferJS);
		
	}
	
	public DatasetID id() {return id;}
	public boolean isTransfer() {return isTransfer;}
	public boolean isKnockout() {return isKnockout;}
	public boolean isInverse() {return isInverse;}
	
	public String targetJPS() {return targetJPS;}
	public String targetSpin() {return targetSpin;}
	public String targetParity() {return targetParity;}
	public int targetParityCode() {return targetParityCode;}
	public int nTargetJPI() {return targetJPIsV.size();}
	
	public boolean isUniqueTargetJPI() {return isUniqueTargetJPI;}
	
	public Vector<JPI> targetJPIsV(){return targetJPIsV;}
	


}

