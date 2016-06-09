//program for testing: performs Sxx, Syy and SSr for a dataset
#include <stdio.h>
#include <string.h>

float getAverage(float var[], int size){
	float mean = 0;
	int index = 0;
	while(index<size){
		mean += var[index];
		index++;
	}
	return mean;
}

float sumMult(float var1[], float var2[], int size){
	float sumSq = 0;
	int index = 0;
	while(index<size){
		sumSq += var1[index]*var2[index];
		index++;
	}
	return sumSq;
}

int main(int argc, char * * argv){
	
	float x[] = {2860, 2010, 2791, 2618, 2212, 2184, 3244, 2692, 2206, 2914, 3034, 4240, 1400, 2257};
	float y[] = {2.66, 2.46, 2.95, 2.81, 2.90, 2.88, 2.13, 3.03, 3.54, 3.1, 4.32, 2.85, 2.2, 2.69};
	int n = 14;	//size of data (since sizeof will work but not keyword)
	float meanx, meany, ssqx, ssqy, sumxy, Sxx, Syy, Sxy, SSr;

	//get the means (try to alter the order to see if outpu changes!)
	meanx = getAverage(x, n);
	meanx /= n;

	meany = getAverage(y, n);
	meany /= n;

	//printf("The averages are: %f, %f\n", meanx, meany);

	//get the sum of the squeares
	ssqx = sumMult(x, x, n);
	ssqy = sumMult(y, y, n);
	sumxy = sumMult(x, y, n);

	//printf("The sum(x²) are x²: %f, %f\n", ssqx, ssqy);

	Sxx = ssqx - n*meanx*meanx;
	Syy = ssqy - n*meany*meany;
	Sxy = sumxy - n*meanx*meany;

	//printf("Sxx, Syy and Sxy: %f, %f, %f\n", Sxx, Syy, Sxy);

	SSr = (Sxx*Syy-Sxy*Sxy)/Sxx;

	printf("SSR = %f", SSr);
	printf("%f", Syy);
	return 0;
}