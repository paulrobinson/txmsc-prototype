package io.narayana.txmsc.ext;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.arjuna.recovery.TransactionStatusConnectionManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;

import java.util.Enumeration;
import java.util.Vector;


/**
 * This class is base class for plug-in modules for the recovery manager.
 * It is responsible for recovering failed subclasses of BasicAction transactions.
 *
 * TO BE CONFIRMED:
 * This class could live in the Narayana code-base once we are sure it is needed. It is a fork
 * of AtomicActionRecoveryModule but appears to define behavior that could be considered
 * generic to BasicAction too.
*/

//TODO: loggers still refer to AtomicAction.

public abstract class BasicActionRecoveryModule implements RecoveryModule
{
   public BasicActionRecoveryModule()
   {
       if (tsLogger.logger.isDebugEnabled()) {
           tsLogger.logger.debug("BasicActionRecoveryModule created");
       }

      if (_recoveryStore == null)
      {
         _recoveryStore = StoreManager.getRecoveryStore();
      }

      _transactionStatusConnectionMgr = new TransactionStatusConnectionManager() ;
   }

   /**
    * This is called periodically by the RecoveryManager
    */
   public void periodicWorkFirstPass()
   {
      // Transaction type
      boolean BasicActions = false ;

      // uids per transaction type
      InputObjectState aa_uids = new InputObjectState() ;

      try
      {
	  if (tsLogger.logger.isDebugEnabled()) {
          tsLogger.logger.debug("BasicActionRecoveryModule first pass");
      }

	  BasicActions = _recoveryStore.allObjUids( _transactionType, aa_uids );

      }
      catch ( ObjectStoreException ex ) {
          tsLogger.i18NLogger.warn_recovery_AtomicActionRecoveryModule_1(ex);
      }

      if ( BasicActions )
      {
         _transactionUidVector = processTransactions( aa_uids ) ;
      }
   }

   public void periodicWorkSecondPass()
   {
       if (tsLogger.logger.isDebugEnabled()) {
           tsLogger.logger.debug("BasicActionRecoveryModule second pass");
       }

       processTransactionsStatus() ;
   }

    protected BasicActionRecoveryModule(String type)
    {
       if (tsLogger.logger.isDebugEnabled()) {
           tsLogger.logger.debug("BasicActionRecoveryModule created");
       }

      if (_recoveryStore == null)
      {
         _recoveryStore = StoreManager.getRecoveryStore();
      }

      _transactionStatusConnectionMgr = new TransactionStatusConnectionManager() ;
      _transactionType = type;

    }

   private void doRecoverTransaction( Uid recoverUid )
   {
      boolean commitThisTransaction = true ;

      // Retrieve the transaction status from its original process.
      int theStatus = _transactionStatusConnectionMgr.getTransactionStatus( _transactionType, recoverUid ) ;

      boolean inFlight = isTransactionInMidFlight( theStatus ) ;

      String Status = ActionStatus.stringForm( theStatus ) ;

      if (tsLogger.logger.isDebugEnabled()) {
          tsLogger.logger.debug("transaction type is " + _transactionType + " uid is " +
                  recoverUid.toString() + "\n ActionStatus is " + Status +
                  " in flight is " + inFlight);
      }

      if ( ! inFlight )
      {
         try
         {
             replayPhase2(recoverUid, theStatus);
         }
         catch ( Exception ex ) {
             tsLogger.i18NLogger.warn_recovery_AtomicActionRecoveryModule_2(recoverUid, ex);
         }
      }
   }

   protected abstract void replayPhase2(Uid recoverUid, int theStatus);

   private boolean isTransactionInMidFlight( int status )
   {
      boolean inFlight = false ;

      switch ( status )
      {
         // these states can only come from a process that is still alive
         case ActionStatus.RUNNING    :
         case ActionStatus.ABORT_ONLY :
         case ActionStatus.PREPARING  :
         case ActionStatus.COMMITTING :
         case ActionStatus.ABORTING   :
         case ActionStatus.PREPARED   :
            inFlight = true ;
            break ;

         // the transaction is apparently still there, but has completed its
         // phase2. should be safe to redo it.
         case ActionStatus.COMMITTED  :
         case ActionStatus.H_COMMIT   :
         case ActionStatus.H_MIXED    :
         case ActionStatus.H_HAZARD   :
         case ActionStatus.ABORTED    :
         case ActionStatus.H_ROLLBACK :
            inFlight = false ;
            break ;

         // this shouldn't happen
         case ActionStatus.INVALID :
         default:
            inFlight = false ;
      }

      return inFlight ;
   }

   private Vector processTransactions( InputObjectState uids )
   {
      Vector uidVector = new Vector() ;

      if (tsLogger.logger.isDebugEnabled()) {
          tsLogger.logger.debug("processing " + _transactionType
                  + " transactions");
      }

      Uid theUid = null;

      boolean moreUids = true ;

      while (moreUids)
      {
         try
         {
             theUid = UidHelper.unpackFrom(uids);

            if (theUid.equals( Uid.nullUid() ))
            {
               moreUids = false;
            }
            else
            {
               Uid newUid = new Uid( theUid ) ;

	       if (tsLogger.logger.isDebugEnabled()) {
               tsLogger.logger.debug("found transaction " + newUid);
           }

               uidVector.addElement( newUid ) ;
            }
         }
         catch ( Exception ex )
         {
            moreUids = false;
         }
      }
      return uidVector ;
   }

   private void processTransactionsStatus()
   {
      // Process the Vector of transaction Uids
      Enumeration transactionUidEnum = _transactionUidVector.elements() ;

      while ( transactionUidEnum.hasMoreElements() )
      {
         Uid currentUid = (Uid) transactionUidEnum.nextElement();

         try
         {
            if ( _recoveryStore.currentState( currentUid, _transactionType ) != StateStatus.OS_UNKNOWN )
            {
               doRecoverTransaction( currentUid ) ;
            }
         }
         catch ( ObjectStoreException ex )
         {
	        tsLogger.i18NLogger.warn_recovery_AtomicActionRecoveryModule_3(currentUid, ex);
         }
      }
   }

   protected abstract String getTransactionType();

    // 'type' within the Object Store.
   private String _transactionType = getTransactionType();

   // Array of transactions found in the object store of the
   // required type.
   private Vector _transactionUidVector = null ;

   // Reference to the Object Store.
   private static RecoveryStore _recoveryStore = null ;

   // This object manages the interface to all TransactionStatusManagers
   // processes(JVMs) on this system/node.
   private TransactionStatusConnectionManager _transactionStatusConnectionMgr ;

}