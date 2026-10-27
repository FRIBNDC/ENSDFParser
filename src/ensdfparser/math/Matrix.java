package ensdfparser.math;

public class Matrix {
	private double[][] A;  //matrix
	private double[] B;    //right side vector
	private int length;
	
	public Matrix(double[][] matrixData, double[] rightValue){
		A=matrixData.clone();
		B=rightValue.clone();
		length=A.length;
	}

	public double[] solve() throws Exception{
		double[] processed=new double[length];
		double max=0;
		double factor=0;
		double temp=0;
		
		int maxRow=0;
		int maxCol=0;
		
		for(int i=0;i<length;i++)
			processed[i]=0;
		
		//main loop
		for(int n=0;n<length;n++){
			max=0;
			
			//loop for looking for a pivot in sub-matrix
			for(int row=0;row<length;row++){
				if(processed[row]!=1){
					for(int col=0;col<length;col++){
						if(processed[col]==0){
							if(Math.abs(A[row][col])>=max){
								max=Math.abs(A[row][col]);
								maxRow=row;
								maxCol=col;
							}
						}
						else if(processed[col]>1)
							throw new Exception("Error: Singular Matrix1!");
					}
				}
			}
			
			processed[maxCol]++;
			//swap two rows to put the pivot element on the diagonal
			if(maxRow!=maxCol){
				vectorSwap(A[maxRow],A[maxCol]);
				temp=B[maxRow];
				B[maxRow]=B[maxCol];
				B[maxCol]=temp;
			}
			
			if(A[maxCol][maxCol]==0)
				throw new Exception("Error: Singular Matrix2!");
			
			factor=1.0/A[maxCol][maxCol];
			B[maxCol]*=factor;
			for(int col=0;col<length;col++)
					A[maxCol][col]*=factor;
				
			for(int row=0;row<length;row++){
				if(row!=maxCol){
					factor=A[row][maxCol];
					B[row]-=B[maxCol]*factor;
					for(int col=0;col<length;col++)
						A[row][col]-=A[maxCol][col]*factor;
				}
			}
			
		}//end main loop

		return B;
	}
	
	public void vectorSwap(double[] v1,double[] v2){
		double temp=0;
		for(int i=0;i<v1.length;i++){
			temp=v1[i];
			v1[i]=v2[i];
			v2[i]=temp;
		}
	}
	
	
	public void print(){
		for(int i=0;i<A.length;i++){
			for(int j=0;j<A.length;j++){
				System.out.print(A[i][j]+" ");
			}
			System.out.println("   "+B[i]);
		}
	}
}
