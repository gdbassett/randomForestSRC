import com.kogalur.randomforest.ModelArg;
import com.kogalur.randomforest.RandomForest;
import com.kogalur.randomforest.RandomForestModel;
import com.kogalur.randomforest.Trace;

import com.kogalur.randomforest.RFLogger;
import com.kogalur.randomforest.RandomForest;

import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.logging.Level;

public class HelloRandomForestSRC {

    static SparkSession spark;
    
    public static void main(String[] args) {
        
        spark = SparkSession
            .builder()
            .appName("HelloRandomForestSRC Example")
            .config("spark.master", "local")
            .getOrCreate();

        // Suppress the Spark logger for all but fatal errors.
        org.apache.log4j.LogManager.getRootLogger().setLevel(org.apache.log4j.Level.FATAL);
        
        runExample();

        spark.stop();
    }

    private static void runExample() {

        ModelArg modelArg;

        String formulaS = new String(" Surv (time, status) ~ .");
        
        String formulaR = new String("mpg ~ . ");
        String formulaM1 = new String("Multivariate (mpg, wt) ~ hp + drat");
        String formulaM2 = new String("Multivariate (mpg, wt) ~ .");
        String formulaC = new String("Species ~ .");
        
        String formulaU = new String(" Unsupervised () ~ .");

        boolean regr, clas, mult1, mult2, unsp, surv;

        // Flags for diagnostic printing.
        regr = clas = mult1 = mult2 = unsp = surv = false;
        
        // Java-side trace.
        Trace.set(15);
        
        if (!true) {

            Dataset<Row> irisDF = spark
                .read()
                .option("header", "true")
                .option("inferSchema", "true") 
                .format("csv")
                .load("./test-classes/data/iris.csv");

            // mtcarsDF.printSchema();
            // mtcarsDF.show();

            modelArg = new ModelArg(formulaC, irisDF);

            clas = true;
            
        }

        if (true) {

            Dataset<Row> mtcarsDF = spark
                .read()
                .option("header", "true")
                .option("inferSchema", "true") 
                .format("csv")
                .load("./test-classes/data/mtcars.csv");

            // mtcarsDF.printSchema();
            // mtcarsDF.show();

            modelArg = new ModelArg(formulaR, mtcarsDF);

            regr = true;

        }

        if (!true) {

            Dataset<Row> wihsDF = spark
                .read()
                .option("header", "true")
                .option("inferSchema", "true") 
                .format("csv")
                .load("/Users/kogalur/Dropbox/working/rfsrc/scala/wihs.csv");

            // wihsDF.printSchema();
            // wihsDF.show();

            modelArg = new ModelArg(formulaS, wihsDF);

            surv = true;
            
            if (!true) {

                double[] newTI = new double[(modelArg.get_timeInterest()).length - 20];

                for (int i = 0; i < (modelArg.get_timeInterest()).length - 20; i++) {
                    newTI[i] = modelArg.get_timeInterest()[i+10] + 1.5;
                }

                newTI[(modelArg.get_timeInterest()).length - 21] = 100;
            
                modelArg.set_timeInterest(newTI);

                modelArg.set_eventWeight(new double[] {1, 3});

                modelArg.set_splitRule("random");
                modelArg.set_nSplit(3);

                modelArg.set_nodeSize(10);
                
            }

            
        }

        modelArg.setEnsembleArg("error", "every.tree");

        // TBD TBD TBD we still need to trim the quant TBD TBD TBD
        modelArg.setEnsembleArg("quantitativeTerminalInfo", "no");

        // Serial or parallel.
        modelArg.set_rfCores(1);

        // Repeatability.
        modelArg.set_seed(-1);

        // We set ntree here.
        modelArg.set_bootstrap(4, "auto", "swr", 0, null, null);

        // Set htry explicitly.
        modelArg.set_htry(0);
        
        // Native-code trace.
        modelArg.set_trace(15 + (1<<13));
       
        RandomForestModel growModel = RandomForest.train(modelArg);


        //  int[][] rmbrMembership = (int[][]) growModel.getEnsemble("rmbrMembership");
        //  growModel.printEnsemble(rmbrMembership);

        if (regr) {
            double[][] perfRegr = (double[][]) growModel.getEnsemble("perfRegr");
            growModel.printEnsemble(perfRegr);
        }
        if (clas) {
            double[][][] perfClas = (double[][][]) growModel.getEnsemble("perfClas");
            growModel.printEnsemble(perfClas);
        }
        
        RFLogger.log(Level.INFO, "\n\nHelloRandomForestSRC() GROW nominal exit.\n\n");

        if (!false) {

            growModel.set_trace(15 + (1<<13));

            if (growModel.getModelArg().get_htry() == 0) {
                growModel.setEnsembleArg("importance", "permute");
                growModel.set_xImportance();
            }
            else {
                growModel.setEnsembleArg("importance", "no");
                growModel.set_xImportance();
            }
            

            growModel.setEnsembleArg("proximity", "oob");

            growModel.setEnsembleArg("error", "every.tree");
            
            RandomForestModel restModel = RandomForest.predict(growModel);

        if (regr) {
            double[][] perfRegr = (double[][]) restModel.getEnsemble("perfRegr");
            restModel.printEnsemble(perfRegr);
        }

            
            RFLogger.log(Level.INFO, "\n\nHelloRandomForestSRC() REST nominal exit.\n\n");
        }
        
        System.out.println("\n\nHelloRandomForestSRC() nominal exit.\n\n");
    }

}
