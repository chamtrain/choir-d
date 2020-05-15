package edu.stanford.registry.server.security;

import edu.stanford.registry.server.security.UserDao.UserAuthority;
import edu.stanford.registry.shared.UserPrincipal;
import edu.stanford.registry.shared.User;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.susom.database.Database;
import com.github.susom.database.RowsHandler;
import com.github.susom.database.SqlSelect;

@RunWith(MockitoJUnitRunner.class)
public class UserDaoTest {
  @InjectMocks
  private UserDao classToTest;

  @Mock
  private Database database;

  private User authenticatedUser;
  private User authorizingUser;

  @Before
  public void setUp() throws Exception {

  }

  @Test
  public void shouldGetUserPrincipal() throws Exception {
    //given
    authenticatedUser = new User(1L, "user1", "Authenticated User", 1l, true);
    authorizingUser = new User(1L, "user2", "Authorizing User", 2l, true);
    SqlSelect toSelect = Mockito.mock(SqlSelect.class);
    UserPrincipal userPrincipal = new UserPrincipal();
    Mockito.when(toSelect.query(ArgumentMatchers.<RowsHandler<UserPrincipal>>any())).thenReturn(userPrincipal);
    Mockito.when(toSelect.argString(Mockito.anyString())).thenReturn(toSelect);
    Mockito.when(database.toSelect(Mockito.anyString())).thenReturn(toSelect);

    //when
    classToTest = new UserDao(database, authenticatedUser, authorizingUser);
    UserPrincipal result = classToTest.findUserPrincipal("user1");

    //expect
    Assert.assertNotNull(result);
    Mockito.verify(database).toSelect(Mockito.anyString());
  }


  @Test
  public void shouldGetAllUsersWithAuthority() throws Exception {
    //given
    SqlSelect toSelect = Mockito.mock(SqlSelect.class);
    UserAuthority[] userAuthorities = {};
    Mockito.when(toSelect.query(ArgumentMatchers.<RowsHandler<UserAuthority[]>>any())).thenReturn(userAuthorities);
    Mockito.when(toSelect.argString(Mockito.anyString())).thenReturn(toSelect);
    Mockito.when(database.toSelect(Mockito.anyString())).thenReturn(toSelect);

    //when
    UserAuthority[] result = classToTest.findAllUserAuthority("username");

    //then
    Assert.assertNotNull(result);
    Mockito.verify(database).toSelect(Mockito.anyString());
  }

  @Test(expected=NullPointerException.class)
  public void shouldNotGetAllUsersWithAuthority() throws Exception {
    //given
    SqlSelect toSelect = Mockito.mock(SqlSelect.class);
    Mockito.when(database.toSelect(Mockito.anyString())).thenReturn(toSelect);

    classToTest = new UserDao(database, authenticatedUser, authorizingUser);

    //when
    classToTest.findAllUserAuthority(null);

    //then throw exception
  }


  @Test
  public void disableUser() throws Exception {

  }

  @Test
  public void grantAuthority() throws Exception {

  }

  @Test
  public void revokeAuthority() throws Exception {

  }

  @Test
  public void findUsersWithDisplayName() throws Exception {

  }

  @Test
  public void findProviders() throws Exception {

  }

  @Test
  public void getProvider() throws Exception {

  }

  @Test
  public void getUserProvider() throws Exception {

  }

  @Test
  public void updateProvider() throws Exception {

  }

  @Test
  public void findDisplayProviders() throws Exception {

  }

  @Test
  public void findUserPreference() throws Exception {

  }

  @Test
  public void findUserPreferences() throws Exception {

  }

  @Test
  public void setUserPreference() throws Exception {

  }

}