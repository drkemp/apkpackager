package org.chromium.aapt;


public enum Errors {
	OK( 0),    // Everything's swell.
    NO_ERROR(0), // No errors.
    UNKNOWN_ERROR(Integer.MIN_VALUE),
    NO_MEMORY(1),//           = -ENOMEM,
    INVALID_OPERATION(2), //   = -ENOSYS,
    BAD_VALUE(3), //          = -EINVAL,
    BAD_TYPE(4),//            = (UNKNOWN_ERROR + 1),
    NAME_NOT_FOUND(5),//      = -ENOENT,
    PERMISSION_DENIED(6),//   = -EPERM,
    NO_INIT(7),//             = -ENODEV,
    ALREADY_EXISTS(8),//      = -EEXIST,
    DEAD_OBJECT (9),//        = -EPIPE,
    FAILED_TRANSACTION(Integer.MIN_VALUE + 2),//UNKNOWN_ERROR
    JPARKS_BROKE_IT(10) ,//    = -EPIPE,
    BAD_INDEX(11)  ,//         = -E2BIG,
    NOT_ENOUGH_DATA(12),//     = (UNKNOWN_ERROR + 3),
    WOULD_BLOCK(13) ,//        = (UNKNOWN_ERROR + 4),
    TIMED_OUT(14) ,//          = (UNKNOWN_ERROR + 5),
    UNKNOWN_TRANSACTION(15),// = (UNKNOWN_ERROR + 6),
    FDS_NOT_ALLOWED(16) ;//    = (UNKNOWN_ERROR + 7);
    
    private final int id;
    Errors(int id) { this.id = id; }
    public int getValue() { return id; }

}
