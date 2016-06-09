#include <stdio.h>
#include <pthread.h>
#include <time.h>
#include <inttypes.h>
#include <stdio.h>
#include <string.h>
 float getAverage ( float var [ ] , int size ) { float mean = 0 ; int index = 0 ; while ( index < size ) { mean += var [ index ] ; index ++ ; } return mean ; } 
 float sumMult ( float var1 [ ] , float var2 [ ] , int size ) { float sumSq = 0 ; int index = 0 ; while ( index < size ) { sumSq += var1 [ index ] * var2 [ index ] ; index ++ ; } return sumSq ; } 

/*Defining function for execution time measurment*/
int64_t timespecDiff(struct timespec *timeA_p, struct timespec *timeB_p)
{
  return ((timeA_p->tv_sec * 1000000000) + timeA_p->tv_nsec) -
           ((timeB_p->tv_sec * 1000000000) + timeB_p->tv_nsec);
}

/* Struct for arguments */
typedef struct sdata_struct{
  void *arg1;
  void *arg2;
  void *arg3;
  void *arg4;
  void *arg5;
  void *arg6;
  void *arg7;
  void *arg8;
  void *arg9;
  void *arg10;
  void *arg11;
  void *arg12;
} sdata_struct;

/*Section 1 thread*/
void *sec_1(void *void_ptr)
{
    sdata_struct *data_str_thread = (sdata_struct*) void_ptr;
    float *meanx = (float *)(data_str_thread->arg11);
    float *x = (float *)(data_str_thread->arg8);
    int *n = (int *)(data_str_thread->arg5);
    (*meanx) = getAverage ( (x) , (*n) ) ; 
    return NULL;
}
/*Section 2 thread*/
void *sec_2(void *void_ptr)
{
    sdata_struct *data_str_thread = (sdata_struct*) void_ptr;
    float *meanx = (float *)(data_str_thread->arg11);
    int *n = (int *)(data_str_thread->arg5);
    (*meanx) /= (*n) ; 
    return NULL;
}
/*Section 3 thread*/
void *sec_3(void *void_ptr)
{
    sdata_struct *data_str_thread = (sdata_struct*) void_ptr;
    float *meany = (float *)(data_str_thread->arg10);
    float *y = (float *)(data_str_thread->arg9);
    int *n = (int *)(data_str_thread->arg5);
    (*meany) = getAverage ( (y) , (*n) ) ; 
    return NULL;
}
/*Section 4 thread*/
void *sec_4(void *void_ptr)
{
    sdata_struct *data_str_thread = (sdata_struct*) void_ptr;
    float *meany = (float *)(data_str_thread->arg10);
    int *n = (int *)(data_str_thread->arg5);
    (*meany) /= (*n) ; 
    return NULL;
}
/*Section 5 thread*/
void *sec_5(void *void_ptr)
{
    sdata_struct *data_str_thread = (sdata_struct*) void_ptr;
    float *ssqx = (float *)(data_str_thread->arg6);
    float *x = (float *)(data_str_thread->arg8);
    int *n = (int *)(data_str_thread->arg5);
    (*ssqx) = sumMult ( (x) , (x) , (*n) ) ; 
    return NULL;
}
/*Section 6 thread*/
void *sec_6(void *void_ptr)
{
    sdata_struct *data_str_thread = (sdata_struct*) void_ptr;
    float *ssqy = (float *)(data_str_thread->arg12);
    float *y = (float *)(data_str_thread->arg9);
    int *n = (int *)(data_str_thread->arg5);
    (*ssqy) = sumMult ( (y) , (y) , (*n) ) ; 
    return NULL;
}
/*Section 7 thread*/
void *sec_7(void *void_ptr)
{
    sdata_struct *data_str_thread = (sdata_struct*) void_ptr;
    float *sumxy = (float *)(data_str_thread->arg1);
    float *x = (float *)(data_str_thread->arg8);
    float *y = (float *)(data_str_thread->arg9);
    int *n = (int *)(data_str_thread->arg5);
    (*sumxy) = sumMult ( (x) , (y) , (*n) ) ; 
    return NULL;
}
/*Section 8 thread*/
void *sec_8(void *void_ptr)
{
    sdata_struct *data_str_thread = (sdata_struct*) void_ptr;
    float *Sxx = (float *)(data_str_thread->arg2);
    float *ssqx = (float *)(data_str_thread->arg6);
    int *n = (int *)(data_str_thread->arg5);
    float *meanx = (float *)(data_str_thread->arg11);
    (*Sxx) = (*ssqx) - (*n) * (*meanx) * (*meanx) ; 
    return NULL;
}
/*Section 9 thread*/
void *sec_9(void *void_ptr)
{
    sdata_struct *data_str_thread = (sdata_struct*) void_ptr;
    float *Syy = (float *)(data_str_thread->arg7);
    float *ssqy = (float *)(data_str_thread->arg12);
    int *n = (int *)(data_str_thread->arg5);
    float *meany = (float *)(data_str_thread->arg10);
    (*Syy) = (*ssqy) - (*n) * (*meany) * (*meany) ; 
    return NULL;
}
/*Section 10 thread*/
void *sec_10(void *void_ptr)
{
    sdata_struct *data_str_thread = (sdata_struct*) void_ptr;
    float *Sxy = (float *)(data_str_thread->arg4);
    float *sumxy = (float *)(data_str_thread->arg1);
    int *n = (int *)(data_str_thread->arg5);
    float *meanx = (float *)(data_str_thread->arg11);
    float *meany = (float *)(data_str_thread->arg10);
    (*Sxy) = (*sumxy) - (*n) * (*meanx) * (*meany) ; 
    return NULL;
}
/*Section 11 thread*/
void *sec_11(void *void_ptr)
{
    sdata_struct *data_str_thread = (sdata_struct*) void_ptr;
    float *SSr = (float *)(data_str_thread->arg3);
    float *Sxx = (float *)(data_str_thread->arg2);
    float *Syy = (float *)(data_str_thread->arg7);
    float *Sxy = (float *)(data_str_thread->arg4);
    (*SSr) = ( (*Sxx) * (*Syy) - (*Sxy) * (*Sxy) ) / (*Sxx) ; 
    return NULL;
}
/*Section 12 thread*/
void *sec_12(void *void_ptr)
{
    sdata_struct *data_str_thread = (sdata_struct*) void_ptr;
    float *SSr = (float *)(data_str_thread->arg3);
    printf ( "SSR = %f" , (*SSr) ) ; 
    return NULL;
}
/*Section 13 thread*/
void *sec_13(void *void_ptr)
{
    sdata_struct *data_str_thread = (sdata_struct*) void_ptr;
    float *Syy = (float *)(data_str_thread->arg7);
    printf ( "%f" , (*Syy) ) ; 
    return NULL;
}

int main()
{
struct timespec start, end;
/* Start execution time measurment */
clock_gettime(CLOCK_MONOTONIC, &start);

 float x [ ] = { 2860 , 2010 , 2791 , 2618 , 2212 , 2184 , 3244 , 2692 , 2206 , 2914 , 3034 , 4240 , 1400 , 2257 } ; 
 float y [ ] = { 2.66 , 2.46 , 2.95 , 2.81 , 2.90 , 2.88 , 2.13 , 3.03 , 3.54 , 3.1 , 4.32 , 2.85 , 2.2 , 2.69 } ; 
 int n = 14 ; 
 float meanx , meany , ssqx , ssqy , sumxy , Sxx , Syy , Sxy , SSr ; 

/*Threads for sections*/
pthread_t sec1_thread;
pthread_t sec2_thread;
pthread_t sec3_thread;
pthread_t sec4_thread;
pthread_t sec5_thread;
pthread_t sec6_thread;
pthread_t sec7_thread;
pthread_t sec8_thread;
pthread_t sec9_thread;
pthread_t sec10_thread;
pthread_t sec11_thread;
pthread_t sec12_thread;
pthread_t sec13_thread;

/*Define our struct for all the variables used*/
struct sdata_struct sdata;
sdata.arg1 = &sumxy;
sdata.arg2 = &Sxx;
sdata.arg3 = &SSr;
sdata.arg4 = &Sxy;
sdata.arg5 = &n;
sdata.arg6 = &ssqx;
sdata.arg7 = &Syy;
sdata.arg8 = &x;
sdata.arg9 = &y;
sdata.arg10 = &meany;
sdata.arg11 = &meanx;
sdata.arg12 = &ssqy;

/*Creating pthreads*/

if (pthread_create (&sec1_thread, NULL, sec_1, (void *)&sdata))
{
    fprintf(stderr, "Error creating thread\n");
    return 1;
} 
pthread_join(sec1_thread, NULL);


if (pthread_create (&sec3_thread, NULL, sec_3, (void *)&sdata))
{
    fprintf(stderr, "Error creating thread\n");
    return 1;
} 
if (pthread_create (&sec2_thread, NULL, sec_2, (void *)&sdata))
{
    fprintf(stderr, "Error creating thread\n");
    return 1;
} 
pthread_join(sec3_thread, NULL);

pthread_join(sec2_thread, NULL);


if (pthread_create (&sec7_thread, NULL, sec_7, (void *)&sdata))
{
    fprintf(stderr, "Error creating thread\n");
    return 1;
} 
if (pthread_create (&sec6_thread, NULL, sec_6, (void *)&sdata))
{
    fprintf(stderr, "Error creating thread\n");
    return 1;
} 
if (pthread_create (&sec5_thread, NULL, sec_5, (void *)&sdata))
{
    fprintf(stderr, "Error creating thread\n");
    return 1;
} 
if (pthread_create (&sec4_thread, NULL, sec_4, (void *)&sdata))
{
    fprintf(stderr, "Error creating thread\n");
    return 1;
} 
pthread_join(sec7_thread, NULL);

pthread_join(sec6_thread, NULL);

pthread_join(sec5_thread, NULL);

pthread_join(sec4_thread, NULL);


if (pthread_create (&sec10_thread, NULL, sec_10, (void *)&sdata))
{
    fprintf(stderr, "Error creating thread\n");
    return 1;
} 
if (pthread_create (&sec9_thread, NULL, sec_9, (void *)&sdata))
{
    fprintf(stderr, "Error creating thread\n");
    return 1;
} 
if (pthread_create (&sec8_thread, NULL, sec_8, (void *)&sdata))
{
    fprintf(stderr, "Error creating thread\n");
    return 1;
} 
pthread_join(sec10_thread, NULL);

pthread_join(sec9_thread, NULL);

pthread_join(sec8_thread, NULL);


if (pthread_create (&sec11_thread, NULL, sec_11, (void *)&sdata))
{
    fprintf(stderr, "Error creating thread\n");
    return 1;
} 
pthread_join(sec11_thread, NULL);


if (pthread_create (&sec13_thread, NULL, sec_13, (void *)&sdata))
{
    fprintf(stderr, "Error creating thread\n");
    return 1;
} 
if (pthread_create (&sec12_thread, NULL, sec_12, (void *)&sdata))
{
    fprintf(stderr, "Error creating thread\n");
    return 1;
} 
pthread_join(sec13_thread, NULL);

pthread_join(sec12_thread, NULL);

/* Finish execution time measurment */
clock_gettime(CLOCK_MONOTONIC, &end);
  uint64_t totalTimeElapsed = timespecDiff(&end, &start);
  printf("\nTime elapsed (in nanoseconds): %" PRIu64 "\n", totalTimeElapsed);
  printf("Time elapsed (in seconds): %f",totalTimeElapsed/1000000000.0);

return 0;
} 